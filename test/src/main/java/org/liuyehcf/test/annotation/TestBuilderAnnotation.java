package org.liuyehcf.test.annotation;

import org.liuyehcf.annotation.source.Builder;

@Builder
public class TestBuilderAnnotation {
    private String firstName;
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public static void main(String[] args) {
        TestBuilderAnnotation.TestBuilderAnnotationBuilder builder=new TestBuilderAnnotation.TestBuilderAnnotationBuilder();
    }
}
