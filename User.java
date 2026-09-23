import java.util.UUID;
import java.io.Serializable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a basic user within the messaging application.
 * <p>
 * A {@code User} stores:
 * <ul>
 *     <li>A unique identifier (UUID)</li>
 *     <li>A display name</li>
 *     <li>A telephone number</li>
 * </ul>
 * This class is extended by {@link Contact}, which adds additional
 * functionality such as group membership.
 */
public class User implements Serializable {

    /** Unique identifier for the user. */
    private UUID id;

    /** The user's display name. */
    private String name;

    /** The user's telephone number. */
    private String number;

    /** Serialization identifier for compatibility. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code User} with the given name and phone number.
     * A unique UUID is automatically generated.
     *
     * @param name   the user's display name
     * @param number the user's telephone number
     */
    public User(String name, String number) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.number = number;
    }

    /**
     * Returns the user's display name.
     *
     * @return the name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the user's telephone number.
     *
     * @return the phone number of the user
     */
    public String getNumber() {
        return number;
    }

    /**
     * Returns the unique identifier of the user.
     *
     * @return the UUID of the user
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the user.
     * <p>
     * This method is rarely needed, as UUIDs are normally immutable.
     *
     * @param id the new UUID to assign
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Updates the user's display name.
     *
     * @param name the new name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Updates the user's telephone number.
     *
     * @param number the new phone number to assign
     */
    public void setNumber(String number) {
        this.number = number;
    }
}