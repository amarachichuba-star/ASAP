import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Represents a contact in the messaging application.
 * <p>
 * A {@code Contact} extends {@link User} and may represent either:
 * <ul>
 *     <li>A normal individual contact</li>
 *     <li>A group chat (containing multiple members)</li>
 * </ul>
 * Groups store a list of member UUIDs, while normal contacts do not.
 */
public class Contact extends User implements Serializable {

    /** True if this contact represents a group chat. */
    private boolean isGroup;

    /** List of UUIDs representing members of the group. */
    private ArrayList<UUID> members;

    /** Serialization identifier for compatibility. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a normal (non-group) contact with a name and phone number.
     *
     * @param name   the contact's name
     * @param number the contact's phone number
     */
    public Contact(String name, String number) {
        super(name, number);
        this.isGroup = false;
        this.members = new ArrayList<>();
    }

    /**
     * Constructs a group contact with the given name.
     * <p>
     * Groups do not have phone numbers, so {@code null} is passed to the
     * superclass constructor.
     *
     * @param name the name of the group
     */
    public Contact(String name) {
        super(name, null);
        this.isGroup = true;
        this.members = new ArrayList<>();
    }

    /**
     * Sets whether this contact represents a group.
     *
     * @param group true if this contact should be treated as a group
     */
    public void setGroup(boolean group) {
        this.isGroup = group;
    }

    /**
     * Returns whether this contact represents a group.
     *
     * @return true if this contact is a group, false otherwise
     */
    public boolean isGroup() {
        return isGroup;
    }

    /**
     * Adds a member to the group using their UUID.
     * Duplicate entries are ignored.
     *
     * @param id the UUID of the member to add
     */
    public void addMember(UUID id) {
        if (!members.contains(id)) {
            members.add(id);
        }
    }

    /**
     * Adds a member to the group using a {@link Contact} object.
     * Duplicate entries are ignored.
     *
     * @param c the contact to add as a member
     */
    public void addMember(Contact c) {
        if (c != null && !members.contains(c.getId())) {
            members.add(c.getId());
        }
    }

    /**
     * Removes a member from the group.
     *
     * @param id the UUID of the member to remove
     */
    public void removeMember(UUID id) {
        members.remove(id);
    }

    /**
     * Returns the list of UUIDs representing the group's members.
     *
     * @return a list of member UUIDs
     */
    public ArrayList<UUID> getMembers() {
        return members;
    }
}