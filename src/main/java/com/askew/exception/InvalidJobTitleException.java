package com.askew.exception;

public class InvalidJobTitleException extends RuntimeException {

    public InvalidJobTitleException(String jobTitle) {
        super("'" + jobTitle + "' is not a recognised job title. Please enter a real job title (e.g. Software Engineer, Data Scientist).");
    }
}
