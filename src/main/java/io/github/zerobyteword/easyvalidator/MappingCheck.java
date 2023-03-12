package io.github.zerobyteword.easyvalidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MappingCheck<S, T> {
    private List<DiffItem> diffItems = new ArrayList<DiffItem>();
    private S source;

    protected MappingCheck(S source, T target) {
        this.source = source;
        this.target = target;
    }

    private T target;


    private static <S, T> MappingCheck<S, T> of(S source, T target) {
        return new MappingCheck<S, T>(source, target);
    }

    public <U> MappingCheck<S, T> assetEquals(Function<? super S, ? extends U> sourceMapper, Function<? super T, ? extends U> targetMapper) {
        Objects.requireNonNull(sourceMapper);
        Objects.requireNonNull(targetMapper);
        if (isPresent()) {
            U left = sourceMapper.apply(source);
            U right = targetMapper.apply(target);
            if (!Objects.equals(left, right)) {
                diffItems.add(new DiffItem("1", "2", left, right));
            }
        }
        return this;
    }

    public <U, W> MappingCheck<S, T> map(Function<? super S, ? extends U> sourceMapper, Function<? super T, ? extends W> targetMapper, SubMappingCheck<U, W> mappingCheck) {
        Objects.requireNonNull(sourceMapper);
        Objects.requireNonNull(targetMapper);
        if (isPresent()) {
            MappingCheck<U, W> that = of(sourceMapper.apply(source), targetMapper.apply(target));
            mappingCheck.equals(that);
            diffItems.addAll(that.get());
        }
        return this;
    }

    private boolean isPresent() {
        return source != null && target != null;
    }


    private List<DiffItem> get() {
        return diffItems;
    }

    @FunctionalInterface
    interface SubMappingCheck<S, T> {
        void equals(MappingCheck<S, T> mappingCheck);
    }
}
