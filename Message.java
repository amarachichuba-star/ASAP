import java.time.Instant;
import java.io.Serializable;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a single message exchanged within a chat.
 * <p>
 * A {@code Message} stores:
 * <ul>
 *     <li>The sender's name</li>
 *     <li>The textual content of the message</li>
 *     <li>A timestamp indicating when the message was created</li>
 *     <li>A flag indicating whether the message has been seen</li>
 *     <li>An optional reaction (e.g., emoji)</li>
 * </ul>
 * Messages are serializable so they can be saved and loaded by {@link FileManager}.
 */
public class Message implements Serializable {

    /** The name of the sender of this message. */
    private String sender;

    /** The textual content of the message. */
    private String content;

    /** The timestamp indicating when the message was created. */
    private Instant timestamp;

    /** True if the message has been seen by the user. */
    private boolean seen;

    /** Optional reaction emoji associated with this message. */
    private String reaction;

    /** Serialization identifier for compatibility. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code Message} with full parameter control.
     * <p>
     * This constructor is primarily used when loading messages from storage.
     *
     * @param sender    the name of the sender
     * @param content   the textual content of the message
     * @param timestamp the timestamp of the message
     * @param seen      whether the message has been seen
     */
    public Message(String sender, String content, Instant timestamp, boolean seen) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.seen = seen;
    }

    /**
     * Constructs a new {@code Message} with the current timestamp and
     * {@code seen = false}. This constructor is used when the user or a
     * contact sends a new message during runtime.
     *
     * @param sender  the name of the sender
     * @param content the textual content of the message
     */
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = Instant.now();
        this.seen = false;
    }

    /**
     * Returns whether the message has been seen.
     *
     * @return true if the message has been seen, false otherwise
     */
    public boolean getSeen() {
        return seen;
    }

    /**
     * Sets the seen status of the message.
     *
     * @param seen true if the message should be marked as seen
     */
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    /**
     * Returns the name of the sender of this message.
     *
     * @return the sender's name
     */
    public String getSender() {
        return sender;
    }

    /**
     * Returns the textual content of the message.
     *
     * @return the message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the timestamp associated with this message.
     *
     * @return the timestamp of the message
     */
    public Instant getTimeStamp() {
        return timestamp;
    }

    /**
     * Returns the reaction associated with this message, if any.
     *
     * @return the reaction emoji, or {@code null} if none is set
     */
    public String getReaction() {
        return reaction;
    }

    /**
     * Sets a reaction emoji for this message.
     *
     * @param reaction the emoji to associate with the message
     */
    public void setReaction(String reaction) {
        this.reaction = reaction;
    }
}