package com.bullseyeaem.core.common.ds;

import com.adobe.granite.ui.components.ds.AbstractDataSource;
import org.apache.commons.collections4.iterators.ListIteratorWrapper;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static org.apache.commons.collections4.IteratorUtils.boundedIterator;

public class PaginatedDataSource extends AbstractDataSource {

    ListIteratorWrapper<Resource> pagingIterator;

    public PaginatedDataSource(Resource resource, Integer offset, Integer limit) {
        this(resource.listChildren(), offset, limit);
    }

    public PaginatedDataSource(Iterator<Resource> resourceIterator, Integer offset, Integer limit) {
        pagingIterator = new ListIteratorWrapper<>(boundedIterator(resourceIterator, offset, limit));
    }

    @Override
    @NotNull
    public Iterator<Resource> iterator() {
        pagingIterator.reset();
        return pagingIterator;
    }
}
