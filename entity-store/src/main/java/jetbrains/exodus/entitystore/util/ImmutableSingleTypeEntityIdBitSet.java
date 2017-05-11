/**
 * Copyright 2010 - 2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.exodus.entitystore.util;

import jetbrains.exodus.core.dataStructures.hash.LongHashSet;
import jetbrains.exodus.core.dataStructures.hash.LongSet;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.PersistentEntityId;
import jetbrains.exodus.entitystore.iterate.EntityIdSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ImmutableSingleTypeEntityIdBitSet implements EntityIdSet {
    private final int singleTypeId;
    private final int size;
    private final long min;
    private final long max;
    private final BitSet data;

    public ImmutableSingleTypeEntityIdBitSet(final int singleTypeId, final long[] source) {
        this.singleTypeId = singleTypeId;
        size = source.length;
        min = source[0];
        max = source[size - 1];
        final long bitsCount = max - min + 1;
        if (min < 0 || bitsCount >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        data = new BitSet((int) bitsCount);
        for (final long value : source) {
            data.set((int) (value - min));
        }
    }

    @Override
    public EntityIdSet add(@Nullable EntityId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityIdSet add(int typeId, long localId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(@Nullable EntityId id) {
        return id != null && contains(id.getTypeId(), id.getLocalId());
    }

    @Override
    public boolean contains(int typeId, long localId) {
        return typeId == singleTypeId
                && localId >= min
                && localId <= max
                && data.get((int) (localId - min));
    }

    @Override
    public boolean remove(@Nullable EntityId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(int typeId, long localId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int count() {
        return size;
    }

    @Override
    public Iterator<EntityId> iterator() {
        return new IdIterator();
    }

    @NotNull
    @Override
    public LongSet getTypeSetSnapshot(int typeId) {
        if (typeId == singleTypeId) {
            LongHashSet result = new LongHashSet(size);
            int next = data.nextSetBit(0);
            while (next != -1) {
                result.add(next + min);
                // if (next == Integer.MAX_VALUE) { break; }
                next = data.nextSetBit(next + 1);
            }
            return result;
        }
        return LongSet.EMPTY;
    }

    class IdIterator implements Iterator<EntityId> {
        int nextBitIndex = data.nextSetBit(0);

        @Override
        public boolean hasNext() {
            return nextBitIndex != -1;
        }

        @Override
        public EntityId next() {
            if (nextBitIndex != -1) {
                final int bitIndex = nextBitIndex;
                nextBitIndex = data.nextSetBit(nextBitIndex + 1);
                return new PersistentEntityId(singleTypeId, bitIndex + min);
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
