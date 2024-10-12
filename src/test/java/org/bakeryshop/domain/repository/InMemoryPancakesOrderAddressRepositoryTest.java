package org.bakeryshop.domain.repository;

import org.bakeryshop.domain.model.address.Building;
import org.bakeryshop.domain.model.order.PancakesOrder;
import org.bakeryshop.domain.model.pancakes.PancakeIngredient;
import org.bakeryshop.domain.repository.order.InMemoryPancakesOrderRepository;
import org.bakeryshop.domain.repository.order.OrderNotFoundException;
import org.bakeryshop.domain.repository.order.PancakesOrderRepository;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryPancakesOrderAddressRepositoryTest {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    private PancakesOrderRepository repository;

    @AfterAll
    static void dispose() {
        executorService.shutdown();
    }

    @BeforeEach
    void prepareTestSubject() {
        repository = new InMemoryPancakesOrderRepository(Duration.ofSeconds(5L));
    }

    @Test
    @DisplayName("create: should create a new PancakesOrder")
    void create() {
        assertThat(repository.create(new Building(1, 2), 2))
                .hasFieldOrPropertyWithValue("address.building.buildingNr", 1)
                .hasFieldOrPropertyWithValue("address.room", 2)
                .matches(it -> Objects.nonNull(it.getId()));
    }

    @Test
    void find() {
        final var pancakesOrder = repository.create(new Building(1, 2), 2);
        assertThat(repository.find(pancakesOrder.getId()))
                .contains(pancakesOrder);
    }

    @Timeout(5)
    @RepeatedTest(value = 256, failureThreshold = 1)
    @DisplayName("create: concurrently pancakes order creation should be ok")
    void createConcurrently() throws Exception {
        final var amountOfOrders = 16;
        assertThat(
                Stream.generate(this::preparePancakeOrderAsync)
                        .limit(amountOfOrders)
                        .map(promise -> promise.thenApply(InMemoryPancakesOrderAddressRepositoryTest::newMutableList))
                        .reduce(CompletableFuture.completedFuture(newMutableList()),
                                (thisPromise, thatPromise) -> thisPromise.thenCombine(thatPromise, (thisList, thatList) -> {
                                    thisList.addAll(thatList);
                                    return thisList;
                                })
                        ).get(5L, TimeUnit.SECONDS)
        )
                .matches(pancakesOrders -> pancakesOrders.size() == amountOfOrders
                        && pancakesOrders.stream()
                        .allMatch(pancakesOrder -> repository.find(pancakesOrder.getId()).isPresent())
                );
    }

    private CompletableFuture<PancakesOrder> preparePancakeOrderAsync() {
        return CompletableFuture.supplyAsync(() ->
                        repository.create(new Building(1, 2), 2),
                executorService
        );
    }

    @Test
    @DisplayName("update: should fail with order not found when order with provided id is mssing")
    void updateShouldFailWithOrderNotFoundWhenOrderIsMissing() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        // exercise
        assertThatThrownBy(() -> repository.update(pancakesOrder))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("update: should be ok when passing the original one")
    void update() {
        // setup
        final var pancakesOrder = repository.create(new Building(1, 2), 2);
        final var amountOfPancakes = 3;
        pancakesOrder.addPancakes(amountOfPancakes, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        // exercise
        assertThat(repository.find(pancakesOrder.getId()).orElseThrow()
                .getPancakes()).hasSize(amountOfPancakes);
        repository.update(pancakesOrder);
        assertThat(repository.find(pancakesOrder.getId()).orElseThrow()
                .getPancakes()).isEqualTo(pancakesOrder.getPancakes())
                .hasSize(amountOfPancakes);
    }

    @Test
    @DisplayName("update: should be ok when passing the copied one")
    void updateWhenPassingUpdateCopyOfGetPancakesOrder() {
        // setup
        final var pancakesOrder = PancakesOrder.copyOf(repository.create(new Building(1, 2), 2));
        final var amountOfPancakes = 3;
        pancakesOrder.addPancakes(amountOfPancakes, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        // exercise
        assertThat(repository.find(pancakesOrder.getId()).orElseThrow()
                .getPancakes()).isEmpty();
        repository.update(pancakesOrder);
        assertThat(repository.find(pancakesOrder.getId()).orElseThrow()
                .getPancakes()).isEqualTo(pancakesOrder.getPancakes())
                .hasSize(amountOfPancakes);
    }

    @Timeout(5)
    @RepeatedTest(value = 256, failureThreshold = 1)
    @DisplayName("update: concurrently update should not lost changes")
    void updateConcurrently() throws Exception {
        // setup
        final var pancakesOrder = repository.create(new Building(1, 2), 2);
        final var amountOfPancakes = 3;
        final var parallelism = 16;
        CompletableFuture.allOf(
                Stream.generate(() -> CompletableFuture.runAsync(() -> {
                            pancakesOrder.addPancakes(amountOfPancakes, Set.of(PancakeIngredient.DARK_CHOCOLATE));
                            repository.update(pancakesOrder);
                        })).limit(parallelism)
                        .toArray(CompletableFuture[]::new)
        ).get(5L, TimeUnit.SECONDS);
        // exercise
        assertThat(repository.find(pancakesOrder.getId()).orElseThrow()
                .getPancakes()).hasSize(parallelism * amountOfPancakes);
    }

    @Test
    @DisplayName("remove: should be ok when removing the existing one")
    void removeWhenRemovingTheExistingOne() {
        // setup
        final var pancakesOrder = repository.create(new Building(1, 2), 2);
        assertThat(repository.remove(pancakesOrder.getId()))
                .contains(pancakesOrder);
        assertThat(repository.find(pancakesOrder.getId()))
                .isEmpty();
    }

    @Timeout(5)
    @RepeatedTest(value = 256, failureThreshold = 1)
    @DisplayName("remove: concurrently removal should be ok only for first one")
    void removeConcurrentlyShouldBeOkOnlyForOne() throws Exception {
        // setup
        final var pancakesOrder = repository.create(new Building(1, 2), 2);
        final var parallelism = 16;
        assertThat(Stream.generate(() -> CompletableFuture.supplyAsync(() ->
                        repository.remove(pancakesOrder.getId())
                                .map(InMemoryPancakesOrderAddressRepositoryTest::newMutableList)
                                .orElseGet(InMemoryPancakesOrderAddressRepositoryTest::newMutableList)
                )).limit(parallelism)
                .reduce(CompletableFuture.completedFuture(newMutableList()),
                        (thisPromise, thatPromise) ->
                                thisPromise.thenCombine(thatPromise, InMemoryPancakesOrderAddressRepositoryTest::addAll)
                ).get(5L, TimeUnit.SECONDS))
                .isEqualTo(newMutableList(pancakesOrder));
        // exercise
        assertThat(repository.find(pancakesOrder.getId()))
                .isEmpty();
    }

    @Test
    void listCompletedOrdersIds() {
        // setup
        final var pancakesOrder = PancakesOrder.copyOf(repository.create(new Building(1, 2), 2));
        repository.update(pancakesOrder);
        assertThat(repository.listCompletedOrdersIds().stream()
                .filter(it -> Objects.equals(pancakesOrder.getId(), it))
                .collect(Collectors.toUnmodifiableSet()))
                .isEmpty();
        pancakesOrder.markAsCompleted();
        repository.update(pancakesOrder);
        // exercise
        assertThat(repository.listCompletedOrdersIds().stream()
                .filter(it -> Objects.equals(pancakesOrder.getId(), it))
                .collect(Collectors.toUnmodifiableSet()))
                .contains(pancakesOrder.getId());

    }

    @Test
    void listPreparedOrdersIds() {
        // setup
        final var pancakesOrder = PancakesOrder.copyOf(repository.create(new Building(1, 2), 2));
        repository.update(pancakesOrder);
        assertThat(repository.listCompletedOrdersIds())
                .isEmpty();
        pancakesOrder.markAsPrepared();
        repository.update(pancakesOrder);
        // exercise
        assertThat(repository.listPreparedOrdersIds())
                .contains(pancakesOrder.getId());
    }

    @SafeVarargs
    private static <T> List<T> newMutableList(T... elems) {
        return new ArrayList<>(Arrays.asList(elems));
    }

    private static <T> List<T> addAll(List<T> thisList, List<T> thatList) {
        thisList.addAll(thatList);
        return thisList;
    }
}