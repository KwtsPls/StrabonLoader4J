package gr.uoa.di.ai.types;

import java.util.Objects;

public class UriContent {
    String rest;
    int namespaceId;

    public String getRest() {
        return rest;
    }

    public void setRest(String rest) {
        this.rest = rest;
    }

    public int getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(int namespaceId) {
        this.namespaceId = namespaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UriContent that = (UriContent) o;
        return namespaceId == that.namespaceId && Objects.equals(rest, that.rest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rest);
    }
}
