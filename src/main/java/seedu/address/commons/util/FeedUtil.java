package seedu.address.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.EntryBook;
import seedu.address.model.entry.Address;
import seedu.address.model.entry.Description;
import seedu.address.model.entry.Entry;
import seedu.address.model.entry.Link;
import seedu.address.model.entry.Title;
import seedu.address.model.entry.exceptions.DuplicateEntryException;
import seedu.address.util.Network;

/**
 * Converts between RSS/Syndication feeds and EntryBook.
 */
public class FeedUtil {
    public static final String DEFAULT_DESCRIPTION_TEXT = "imported from %s";
    public static final String DEFAULT_ADDRESS_TEXT = "this field is deprecated";

    private static final Logger logger = LogsCenter.getLogger(FeedUtil.class);

    /** Reads in URL of a feed and serializes it into an {@code EntryBook}. */
    public static EntryBook fromFeedUrl(String feedUrl) throws IOException, FeedException {
        InputStream inputStream = Network.fetchAsStream(feedUrl);
        SyndFeed syndFeed = new SyndFeedInput().build(new XmlReader(inputStream));

        List<Entry> importedEntries = syndFeed.getEntries().stream()
                .flatMap(syndEntry -> syndEntryToEntryBookEntry(syndEntry, feedUrl).stream())
                .collect(Collectors.toList());
        EntryBook entryBook = new EntryBook();
        for (Entry entry : importedEntries) {
            try {
                entryBook.addEntry(entry);
            } catch (DuplicateEntryException dee) {
                logger.warning("Entry " + entry
                        + " duplicates earlier entry and has been discarded while importing entries from " + feedUrl);
            }
        }
        return entryBook;
    }

    /** Converts a single SyndEntry into an EntryBook Entry. */
    private static Optional<Entry> syndEntryToEntryBookEntry(SyndEntry syndEntry, String feedUrl) {
        Optional<String> link = Optional.ofNullable(syndEntry.getLink());
        if (!link.isPresent()) {
            logger.warning("Entry without link found when processing " + feedUrl + ", discarding.");
            return Optional.empty();
        }
        return Optional.of(new Entry(
                extractTitle(syndEntry),
                extractDescription(syndEntry, feedUrl),
                new Link(link.get()),
                new Address(DEFAULT_ADDRESS_TEXT),
                Collections.emptySet()
        ));
    }

    /** Extracts title from syndication entry. */
    private static Title extractTitle(SyndEntry syndEntry) {
        // defend against nulls from library
        Optional<String> title = Optional.ofNullable(syndEntry.getTitle())
                .map(String::trim);
        return new Title(title.orElse(""));
    }

    /** Extracts a useful description from a SyndEntry. */
    private static Description extractDescription(SyndEntry syndEntry, String feedUrl) {
        // note that both SyndEntry#getDescription and SyndContent#getValue might null
        Optional<String> description = Optional.ofNullable(syndEntry.getDescription())
                .flatMap(syndContent -> Optional.ofNullable(syndContent.getValue()))
                .map(desc -> Jsoup.parseBodyFragment(desc).body().text().replace('\n', ' ').trim())
                .filter(s -> !s.isEmpty());

        return new Description(description.orElse(String.format(DEFAULT_DESCRIPTION_TEXT, feedUrl)));
    }
}