# Capability: shared-pieces

What the shared Godot addon guarantees to every engine game beyond the maths: saved
settings, sound switches, the level map, colour checks for colour blindness, crowd
formations and the About card.

- **Source:** `engine/core/addons/lillo_core/` (`save/`, `ui/`, `crowd/`)
- **Verified by:** `engine/core/tests/` (`test_settings`, `test_audio`, `test_level_map`,
  `test_colours`, `test_formation`, `test_about`)

---

### Requirement: Settings and sound switches are kept

A setting SHALL read back what was written and its fallback when never written. The sound,
music and vibration switches SHALL be saved; no effect SHALL play with sound off, and the
same effect SHALL not repeat inside its minimum gap.

#### Scenario: settings and audio
- **WHEN** a key was never written **THEN** it reads its fallback; once written, it reads the value
- **WHEN** sound, music and vibration are switched off **THEN** all three are saved off
- **WHEN** an effect is played twice at once, or with sound off **THEN** the second is skipped, or none plays
- *Verifies:* `test_an_unset_key_reads_its_fallback`, `test_a_written_key_reads_back`,
  `test_switches_are_saved`, `test_an_effect_is_throttled`, `test_nothing_plays_with_sound_off`

### Requirement: The level map opens where the player left off

The map SHALL point at the last open level as the next to play and open on its page; pages
SHALL stay in range; a tile SHALL emit its number, and a locked tile SHALL be disabled.

#### Scenario: level map
- **WHEN** 7 levels are open **THEN** the next to play is 7, and level 11's tile is disabled
- **WHEN** 18 are open and the map opens **THEN** it shows the second page
- **WHEN** the map is turned before the first or past the last page **THEN** it stays on the first or last
- **WHEN** tile 1 is pressed **THEN** the map emits `picked(1)`
- *Verifies:* `test_the_next_level_to_play_is_the_last_open_one`,
  `test_it_opens_on_the_page_of_the_next_level`, `test_pages_stay_in_range`, `test_a_tile_emits_its_number`

### Requirement: Colour differences account for colour blindness

The colour difference SHALL be zero for a colour against itself, and the worst difference
over every kind of vision SHALL be large for black and white; red and green SHALL come out
much closer for deuteranopia than for normal vision, so palette tests catch them.

#### Scenario: colour checks
- **WHEN** teal is compared with itself, and black with white **THEN** ΔE is 0, and above 90 for everyone
- **WHEN** a red and a green are compared **THEN** ΔE is above 50 normally and under half that for deuteranopia
- *Verifies:* `test_a_colour_does_not_differ_from_itself`, `test_black_and_white_are_far_apart_for_everyone`,
  `test_red_and_green_merge_for_red_green_blindness`

### Requirement: A crowd stands on the track without overlapping

A formation SHALL give one place per member, the leader's for a lone member, every place
within the track and behind the leader, no two members closer than their spacing; a scatter
SHALL be stable for its seed and bounded.

#### Scenario: formations
- **WHEN** formations of 0, 1 and 250 are made **THEN** they have 0, 1 and 250 places, the single one at the leader
- **WHEN** 1,000 stand on a 6 m track **THEN** all are within 3 m of the middle and behind the leader
- **WHEN** 60 stand with 0.5 spacing **THEN** no two are closer than 0.49
- **WHEN** 40 are scattered twice with seed 7 **THEN** both are the same and each moved at most 0.2
- *Verifies:* `test_one_offset_per_member`, `test_a_lone_member_stands_on_the_leader`,
  `test_a_big_crowd_never_spills_off_the_track`, `test_members_do_not_overlap`,
  `test_scatter_is_stable_and_bounded`

### Requirement: The About card links the privacy policy

An About card given a privacy policy SHALL show a link named for it (opening its address in
the browser) after the credits' links; without one it SHALL show only the credits' links.

#### Scenario: privacy link
- **WHEN** a card is made with ["Privacy policy", an address] **THEN** its links read the credit's address, then "Privacy policy"
- **WHEN** a card is made without one **THEN** its only link is the credit's address
- *Verifies:* `test_the_privacy_policy_link_shows_its_name`, `test_without_a_privacy_policy_only_the_credits_link`
