import java.util.LinkedList;
import java.util.UUID;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Represents a chat thread between the user and a specific contact or group.
 * <p>
 * A {@code Chat} stores:
 * <ul>
 *     <li>The UUID of the associated {@link Contact}</li>
 *     <li>A list of {@link Message} objects in chronological order</li>
 *     <li>A flag indicating whether the chat contains unread messages</li>
 * </ul>
 * Chats are serializable so they can be saved and restored by {@link FileManager}.
 */
public class Chat implements Serializable {

    /** The UUID of the contact or group associated with this chat. */
    private UUID contactId;

    /** Ordered list of messages in this chat. */
    private LinkedList<Message> messages;

    /** True if the chat contains at least one unread message. */
    private boolean hasUnread;

    /** Serialization identifier for compatibility. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code Chat} associated with the given contact ID.
     * The chat initially contains no messages and is marked as having no unread content.
     *
     * @param contactId the UUID of the contact or group this chat belongs to
     */
    public Chat(UUID contactId) {
        this.contactId = contactId;
        this.messages = new LinkedList<>();
        this.hasUnread = false;
    }

    /**
     * Returns the UUID of the contact associated with this chat.
     *
     * @return the contact's UUID
     */
    public UUID getContactId() {
        return contactId;
    }

    /**
     * Returns the list of messages in this chat.
     * <p>
     * Messages are stored in chronological order, with the earliest message first.
     *
     * @return a {@link LinkedList} of messages
     */
    public LinkedList<Message> getMessages() {
        return messages;
    }

    /**
     * Returns whether the chat contains unread messages.
     *
     * @return true if at least one message is unread, false otherwise
     */
    public boolean getHasUnread() {
        return hasUnread;
    }

    /**
     * Recalculates the unread status of the chat by scanning all messages.
     * <p>
     * If any message is marked as unseen, the chat is considered unread.
     */
    public void updateHasUnread() {
        hasUnread = false;
        for (Message m : messages) {
            if (!m.getSeen()) {
                hasUnread = true;
                break;
            }
        }
    }

    /**
     * Adds a new message to the chat.
     * <p>
     * If the message is not marked as seen, the chat is flagged as unread.
     *
     * @param msg the message to add
     */
    public void addMessage(Message msg) {
        messages.add(msg);
        if (!msg.getSeen()) {
            hasUnread = true;
        }
    }

    /**
     * Marks all messages in the chat as seen and clears the unread flag.
     */
    public void markAllAsSeen() {
        for (Message m : messages) {
            m.setSeen(true);
        }
        hasUnread = false;
    }

    /**
     * Returns the most recent message in the chat.
     *
     * @return the last message, or {@code null} if the chat is empty
     */
    public Message getLastMessage() {
        if (messages.isEmpty()) return null;
        return messages.getLast();
    }

    /**
     * Returns a short preview of the most recent message.
     * <p>
     * If the message exceeds 30 characters, it is truncated with an ellipsis.
     *
     * @return a preview string, or an empty string if the chat has no messages
     */
    public String getLastMessagePreview() {
        Message last = getLastMessage();
        if (last == null) return "";

        String content = last.getContent();
        int maxLen = 30;

        return content.length() <= maxLen
                ? content
                : content.substring(0, maxLen) + "...";
    }

    /**
     * Returns the timestamp of the most recent message formatted as HH:mm.
     *
     * @return a formatted time string, or an empty string if the chat has no messages
     */
    public String getLastMessageTimeFormatted() {
        Message last = getLastMessage();
        if (last == null) return "";

        Instant timestamp = last.getTimeStamp();
        LocalDateTime time = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return time.format(formatter);
    }
}