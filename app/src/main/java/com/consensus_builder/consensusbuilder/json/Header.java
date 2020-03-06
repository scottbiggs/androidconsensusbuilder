package com.consensus_builder.consensusbuilder.json;

/**
 * Aligns with the Header portion of our JSON data
 * object.
 */
@SuppressWarnings("unused")
public class Header {

    /** Presumable the id of the client */
    public String id;

    /** Allows for a title at the top of the screen */
    public String title;

    /**
     * The number of the user's account. Why not just use the
     * id? perhaps to allow for people to have multiple devices.
     * I'm presuming that each device will have its id, whereas
     * each user will have just one account.
     */
    public String user_account;

    /** Date the account expires */
    public String expires;

    /** Multiple security levels allowed.  todo: details for security levels */
    public int security_level;

    /** If false, hide the display name */
    public boolean show_display_names;  // todo: differs from specs

    /**
     * The total number of queries in this exchange.
     * Should equal the number of elements in the question array.
     */
    public int number_questions;

    /** Constructor */
    public Header() {}

}
