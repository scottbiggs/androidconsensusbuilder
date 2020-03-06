package com.consensus_builder.consensusbuilder.json;

/**
 * Data structure for a Response to a question.  Mirrors
 * the JSON of the same name.
 *
 * todo:  this class may not be needed
 */
public class Response {

    /** The response number--should match the Question number! */
    public int number;

    /**
     * What the responder typed.
     *
     * todo:  Does not take into account radios, checkboxes, and ranking well.
     */
    public String answer;


    public Response() {}
}
