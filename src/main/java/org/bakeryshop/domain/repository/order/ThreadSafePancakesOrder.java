package org.bakeryshop.domain.repository.order;

import org.bakeryshop.common.TimeoutException;
import org.bakeryshop.domain.model.address.OrderAddress;
import org.bakeryshop.domain.model.order.OrderState;
import org.bakeryshop.domain.model.order.PancakesOrder;
import org.bakeryshop.domain.model.order.PancakesOrderSnapshot;
import org.bakeryshop.domain.model.pancakes.PancakeIngredient;
import org.bakeryshop.domain.model.pancakes.PancakeRecipe;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

final class ThreadSafePancakesOrder implements PancakesOrder {

    private final Lock readLock;
    private final Lock writeLock;

    private final PancakesOrder delegate;
    private final Duration lockTimeout;

    ThreadSafePancakesOrder(PancakesOrder delegate, Duration lockTimeout) {
        this.delegate = delegate;
        this.lockTimeout = lockTimeout;
        final var readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
    }

    @Override
    public PancakesOrder addPancakes(int count, Set<PancakeIngredient> ingredients) {
        runWithAcquiringWriteLock(() -> delegate.addPancakes(count, ingredients));
        return this;
    }

    @Override
    public PancakesOrder removePancakes(String description, int count) {
        runWithAcquiringWriteLock(() -> delegate.removePancakes(description, count));
        return this;
    }

    @Override
    public PancakesOrder handleOrderCanceled() {
        supplyWithAcquiringReadLock(() -> {
            delegate.handleOrderCanceled();
            return null;
        });
        return this;
    }

    @Override
    public List<String> pancakeDescriptions() {
        return supplyWithAcquiringReadLock(delegate::pancakeDescriptions);
    }

    @Override
    public PancakesOrderSnapshot snapshot() {
        return supplyWithAcquiringReadLock(delegate::snapshot);
    }

    @Override
    public boolean isPrepared() {
        return supplyWithAcquiringReadLock(delegate::isPrepared);
    }

    @Override
    public boolean isCompleted() {
        return supplyWithAcquiringReadLock(delegate::isCompleted);
    }

    @Override
    public PancakesOrder markAsPrepared() {
        runWithAcquiringWriteLock(delegate::markAsPrepared);
        return this;
    }

    @Override
    public PancakesOrder markAsCompleted() {
        runWithAcquiringWriteLock(delegate::markAsCompleted);
        return this;
    }

    @Override
    public OrderAddress getAddress() {
        return delegate.getAddress();
    }

    @Override
    public OrderState getState() {
        return delegate.getState();
    }

    @Override
    public List<PancakeRecipe> getPancakes() {
        return supplyWithAcquiringReadLock(delegate::getPancakes);
    }

    @Override
    public UUID getId() {
        return delegate.getId();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PancakesOrder that)) return false;
        return PancakesOrder.equals(delegate, that);
    }

    @Override
    public int hashCode() {
        return PancakesOrder.hashCode(delegate);
    }

    private void runWithAcquiringWriteLock(Runnable runnable) {
        try {
            if (writeLock.tryLock(lockTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                runnable.run();
            } else {
                throw new TimeoutException("Timeout while getting write lock.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while getting write lock.");
        } finally {
            writeLock.unlock();
        }
    }

    private <T> T supplyWithAcquiringReadLock(Supplier<T> supplier) {
        try {
            if (readLock.tryLock(lockTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                return supplier.get();
            } else {
                throw new TimeoutException("Timeout while getting read lock.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while getting read lock.");
        } finally {
            readLock.unlock();
        }
    }
}
