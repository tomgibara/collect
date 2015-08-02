package com.tomgibara.collect;

import java.util.Objects;
import com.tomgibara.hashing.Hasher;
import com.tomgibara.hashing.Hashing;

class EquRels {

	static final EquRel<?> EQUALITY = new EquRel<Object>() {

		@Override
		public boolean isEquivalent(Object e1, Object e2) {
			return Objects.equals(e1, e2);
		}

	};

	static final EquRel<?> IDENTITY = new EquRel<Object>() {

		@Override
		public boolean isEquivalent(Object e1, Object e2) {
			return e1 == e2;
		}
		
		@Override
		public Hasher<Object> getHasher() {
			return Hashing.identityHasher();
		}

	};

}
