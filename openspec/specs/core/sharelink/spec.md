# Capability: sharelink

A small payload (a word pack, a tournament, a challenge) travels inside a link or QR code
with no server: the data is after the `#`, compressed.

- **Source:** `core/sharelink/` (`ShareLink`)
- **Verified by:** `core/sharelink/src/test/` (`ShareLinkTest`)

---

### Requirement: Content round-trips through every form and is compressed

A payload SHALL round-trip through its code, its link, a message holding the link (with
emoji around it), the bare code, and the website's `?d=` form. A repetitive payload SHALL
yield a link shorter than 300 characters. Empty text, text that is not base64, a code with
its last 10 characters dropped, a link to another site, and a link of another path SHALL
read as nothing.

#### Scenario: links
- **WHEN** a payload is encoded **THEN** its code, link, message with the link, bare code and `?d=` website form all decode to it
- **WHEN** the payload is repetitive **THEN** its link is under 300 characters
- **WHEN** the text is empty, `1notbase64!!`, a truncated code, an `example.com` link, or a link made for another path **THEN** the result is null
- *Verifies:* `roundTripsThroughEveryForm`, `compressesRepetitiveContent`,
  `garbageAndOtherPagesAreNull`
