package org.borman.dto.page;

import org.borman.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@XmlRootElement(name = "page")
public class SerializableAddressPage implements Page<Address> {

    private boolean hasNext;
    private boolean hasPrevious;
    private Pageable nextPageable;
    private Pageable previousPageable;
    private Iterator<Address> iterator;
    private boolean hasContent;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;
    private int numberOfElements;
    private List<Address> content = new ArrayList<Address>();
    private boolean last;
    private boolean first;
    private Sort sort;

    protected SerializableAddressPage() {
    }

    public SerializableAddressPage(Page<Address> basePage) {
        this.totalPages = basePage.getTotalPages();
        this.totalElements = basePage.getTotalElements();
        this.number = basePage.getNumber();
        this.size = basePage.getSize();
        this.numberOfElements = basePage.getNumberOfElements();
        this.content = basePage.getContent();
        this.first = basePage.isFirst();
        this.last = basePage.isLast();
        this.sort = basePage.getSort();
        this.hasContent = basePage.hasContent();
        this.hasNext = basePage.hasNext();
        this.hasPrevious = basePage.hasPrevious();
        this.nextPageable = basePage.nextPageable();
        this.previousPageable = basePage.previousPageable();
        this.iterator = basePage.iterator();
    }


    @Override
    @XmlElement
    public int getTotalPages() {
        return totalPages;
    }

    @Override
    @XmlElement
    public long getTotalElements() {
        return totalElements;
    }

    @Override
    @XmlElement
    public int getNumber() {
        return number;
    }

    @Override
    @XmlElement
    public int getSize() {
        return size;
    }

    @Override
    @XmlElement
    public int getNumberOfElements() {
        return numberOfElements;
    }

    @Override
    @XmlElement
    public List<Address> getContent() {
        return content;
    }

    @Override
    public boolean hasContent() {
        return hasContent;
    }

    @Override
    @XmlElement
    public Sort getSort() {
        return sort;
    }

    @Override
    @XmlElement
    public boolean isFirst() {
        return first;
    }

    @Override
    @XmlElement
    public boolean isLast() {
        return last;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public boolean hasPrevious() {
        return hasPrevious;
    }

    @Override
    public Pageable nextPageable() {
        return nextPageable;
    }

    @Override
    public Pageable previousPageable() {
        return previousPageable;
    }

    @Override
    public Iterator<Address> iterator() {
        return iterator;
    }

    public void setContent(List<Address> content) {
        this.content = content;
    }

    protected void setFirst(boolean first) {
        this.first = first;
    }

    protected void setLast(boolean last) {
        this.last = last;
    }

    protected void setNumber(int number) {
        this.number = number;
    }

    protected void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    protected void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    protected void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    protected void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }

    protected void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    protected void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    protected void setIterator(Iterator<Address> iterator) {
        this.iterator = iterator;
    }

    protected void setNextPageable(Pageable nextPageable) {
        this.nextPageable = nextPageable;
    }

    protected void setPreviousPageable(Pageable previousPageable) {
        this.previousPageable = previousPageable;
    }
}
