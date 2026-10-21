package com.jorgelillo.whoslying.domain

/** English built-in packs. Each line: word / similar word / similar word. */
internal val englishPacks = listOf(
    WordPacks.pack(
        "food",
        "Food",
        "🍕",
        """
        Pizza / Burger / Hot dog
        Paella / Risotto / Jambalaya
        Omelette / Scrambled eggs / Quiche
        Pancakes / Waffles / French toast
        Fried chicken / Chicken nuggets / Chicken wings
        Soup / Stew / Chili
        Sushi / Ramen / Dumplings
        Ice cream / Frozen yogurt / Popsicle
        Chocolate / Caramel / Fudge
        Coffee / Tea / Hot chocolate
        Orange / Tangerine / Grapefruit
        Strawberry / Cherry / Raspberry
        Bacon / Sausage / Ham
        Cheese / Yogurt / Butter
        Bread / Toast / Bagel
        French fries / Nachos / Onion rings
        Lentils / Chickpeas / Beans
        Cake / Pie / Cheesecake
        Popcorn / Pretzels / Peanuts
        Olives / Pickles / Capers
        Spaghetti / Macaroni / Lasagna
        Banana / Mango / Pineapple
        Watermelon / Melon / Papaya
        Roast turkey / Roast chicken / Duck
        Fish and chips / Calamari / Shrimp
        Sandwich / Wrap / Burrito
        Salad / Coleslaw / Grilled vegetables
        Croissant / Muffin / Cupcake
        Cereal / Oatmeal / Granola
        Lemonade / Orange juice / Milkshake
        """
    ),
    WordPacks.pack(
        "places",
        "World & places",
        "🗺️",
        """
        Beach / Swimming pool / Lake
        Cinema / Theater / Concert hall
        Hospital / Clinic / Pharmacy
        Airport / Train station / Harbor
        Supermarket / Market / Corner shop
        Library / Bookshop / Archive
        Gym / Sports center / Indoor pool
        Nightclub / Bar / Karaoke
        Zoo / Aquarium / Farm
        Museum / Art gallery / Castle
        Church / Cathedral / Monastery
        Mountain / Hill / Volcano
        Desert / Savanna / Steppe
        Jungle / Forest / Swamp
        Theme park / Funfair / Water park
        Restaurant / Café / Food truck
        Hotel / Hostel / Campsite
        Paris / London / Rome
        New York / Los Angeles / Chicago
        Japan / China / South Korea
        Egypt / Morocco / Tunisia
        Brazil / Argentina / Mexico
        Madrid / Barcelona / Lisbon
        Antarctica / North Pole / Greenland
        Desert island / Shipwreck / Lighthouse
        Prison / Police station / Courthouse
        University / High school / Academy
        Hair salon / Barbershop / Beauty salon
        City hall / Post office / Bank
        Stadium / Arena / Racetrack
        Gas station / Garage / Car wash
        """
    ),
    WordPacks.pack(
        "animals",
        "Animals & nature",
        "🦁",
        """
        Lion / Tiger / Leopard
        Dog / Wolf / Fox
        Cat / Lynx / Ferret
        Dolphin / Whale / Shark
        Elephant / Rhino / Hippo
        Giraffe / Zebra / Camel
        Penguin / Seal / Walrus
        Eagle / Falcon / Vulture
        Parrot / Toucan / Budgie
        Snake / Lizard / Crocodile
        Frog / Toad / Salamander
        Butterfly / Moth / Dragonfly
        Bee / Wasp / Fly
        Ant / Termite / Cockroach
        Horse / Donkey / Pony
        Cow / Bull / Buffalo
        Sheep / Goat / Llama
        Pig / Wild boar / Badger
        Hen / Duck / Goose
        Bear / Panda / Koala
        Monkey / Gorilla / Chimpanzee
        Kangaroo / Hare / Squirrel
        Octopus / Squid / Jellyfish
        Turtle / Snail / Armadillo
        Owl / Bat / Raven
        Sunflower / Daisy / Tulip
        Rose / Carnation / Orchid
        Cactus / Aloe vera / Palm tree
        Rainbow / Northern lights / Eclipse
        Thunderstorm / Tornado / Hurricane
        Waterfall / River / Fountain
        Oak / Pine / Willow
        """
    ),
    WordPacks.pack(
        "home",
        "Everyday life",
        "☕",
        """
        Sofa / Armchair / Beanbag
        Bed / Bunk bed / Hammock
        Fridge / Freezer / Pantry
        Microwave / Oven / Toaster
        Washing machine / Dryer / Dishwasher
        Shower / Bathtub / Sink
        Mirror / Window / Painting
        Umbrella / Raincoat / Parasol
        Keys / Padlock / Lock
        Remote control / Keyboard / Light switch
        Pillow / Cushion / Teddy bear
        Blanket / Duvet / Towel
        Toothbrush / Comb / Dental floss
        Alarm clock / Watch / Kitchen timer
        Broom / Mop / Vacuum cleaner
        Candle / Lamp / Flashlight
        Houseplant / Vase / Flowerpot
        Garbage / Recycling / Compost
        Nap / Early morning / Insomnia
        Grocery shopping / Laundry / Cooking
        Traffic jam / Supermarket queue / Waiting room
        Monday / Sunday / Friday
        Birthday / Anniversary / Graduation
        Moving house / Renovation / Spring cleaning
        Neighbor / Doorman / Landlord
        Mailbox / Doorbell / Intercom
        Curtain / Blinds / Shutters
        Clothesline / Iron / Hanger
        Mug / Glass / Thermos
        Fork / Spoon / Chopsticks
        """
    ),
    WordPacks.pack(
        "jobs",
        "Jobs",
        "👩‍🚒",
        """
        Firefighter / Police officer / Lifeguard
        Doctor / Nurse / Vet
        Teacher / Camp counselor / Coach
        Chef / Waiter / Baker
        Pilot / Flight attendant / Air traffic controller
        Astronaut / Astronomer / Scientist
        Lawyer / Judge / Notary
        Hairdresser / Makeup artist / Stylist
        Plumber / Electrician / Bricklayer
        Journalist / TV host / Radio DJ
        Photographer / Painter / Designer
        Actor / Singer / Dancer
        Architect / Engineer / Surveyor
        Farmer / Fisherman / Gardener
        Mail carrier / Delivery driver / Courier
        Dentist / Optician / Physiotherapist
        Programmer / IT technician / YouTuber
        Detective / Spy / Bodyguard
        Magician / Clown / Juggler
        Taxi driver / Truck driver / Bus driver
        Carpenter / Blacksmith / Sculptor
        Banker / Accountant / Cashier
        Footballer / Referee / Goalkeeper
        Soldier / Sailor / Captain
        Pharmacist / Chemist / Biologist
        Psychologist / Psychiatrist / Life coach
        Mechanic / Panel beater / Gas station attendant
        Pastry chef / Ice cream maker / Chocolatier
        Tour guide / Receptionist / Travel agent
        Shop assistant / Cashier / Shelf stacker
        """
    ),
    WordPacks.pack(
        "sports",
        "Sports",
        "🏅",
        """
        Rugby / American football / Lacrosse
        Basketball / Handball / Volleyball
        Tennis / Padel / Badminton
        Swimming / Water polo / Diving
        Cycling / Mountain biking / Spinning
        Skiing / Snowboarding / Ice skating
        Surfing / Windsurfing / Paddleboarding
        Boxing / Karate / Judo
        Golf / Mini golf / Croquet
        Athletics / Marathon / Triathlon
        Gymnastics / Yoga / Pilates
        Climbing / Hiking / Abseiling
        Formula 1 / MotoGP / Rally
        Baseball / Cricket / Softball
        Ice hockey / Curling / Figure skating
        Horse riding / Polo / Rodeo
        Archery / Darts / Shooting
        Fencing / Kendo / Wrestling
        Rowing / Canoeing / Sailing
        Skateboarding / Roller skating / Scooter
        Ping-pong / Squash / Racquetball
        Skydiving / Bungee jumping / Paragliding
        Olympic Games / World Cup / Super Bowl
        Gold medal / Trophy / Podium
        Race number / Whistle / Stopwatch
        Warm-up / Stretching / Sore muscles
        Scuba diving / Snorkeling / Freediving
        Bowling / Pool / Table football
        Zumba / Aerobics / CrossFit
        Sumo / Arm wrestling / Tug of war
        """
    ),
    WordPacks.pack(
        "football",
        "Football",
        "⚽",
        """
        Goal / Penalty / Free kick
        Goalkeeper / Defender / Striker
        Red card / Yellow card / Sent off
        Offside / Handball / VAR
        Corner / Throw-in / Goal kick
        Real Madrid / Barcelona / Atlético Madrid
        Messi / Cristiano Ronaldo / Neymar
        World Cup / Euros / Copa América
        Champions League / Europa League / Premier League
        Ballon d'Or / Golden Boot / Golden Glove
        Manager / Club president / Agent
        Transfer / Loan / Free agent
        Derby / El Clásico / Cup final
        Extra time / Penalty shootout / Half-time
        Bicycle kick / Scissor kick / Backheel
        Team jersey / Scarf / Pennant
        Fans / Ultras / Supporters' club
        Referee / Linesman / Fourth official
        Captain's armband / Shirt number / Shin pads
        Hat-trick / Brace / Own goal
        Dressing room / Bench / Tunnel
        Line-up / Tactics / Whiteboard
        Top scorer / Top assister / Player of the match
        Promotion / Relegation / Playoff
        Futsal / Beach soccer / Five-a-side
        Pelé / Maradona / Zidane
        Iniesta / Xavi / Busquets
        Lamine Yamal / Pedri / Gavi
        Mbappé / Haaland / Vinícius
        Manchester United / Liverpool / Chelsea
        """
    ),
    WordPacks.pack(
        "movies",
        "Movies & TV",
        "🎬",
        """
        Titanic / Avatar / Pearl Harbor
        Harry Potter / The Lord of the Rings / The Chronicles of Narnia
        Star Wars / Star Trek / Guardians of the Galaxy
        Spider-Man / Batman / Superman
        Frozen / Tangled / Moana
        The Lion King / The Jungle Book / Tarzan
        Toy Story / Cars / Monsters, Inc.
        Shrek / Madagascar / Kung Fu Panda
        Jurassic Park / King Kong / Godzilla
        Pirates of the Caribbean / Indiana Jones / The Mummy
        The Simpsons / Family Guy / Futurama
        Friends / How I Met Your Mother / The Big Bang Theory
        Money Heist / Elite / Prison Break
        Stranger Things / Dark / The Goonies
        Game of Thrones / House of the Dragon / The Witcher
        Breaking Bad / Narcos / Better Call Saul
        Squid Game / Alice in Borderland / The Hunger Games
        Fast & Furious / Gone in 60 Seconds / Baby Driver
        The Matrix / Inception / Terminator
        Back to the Future / Interstellar / Groundhog Day
        Mamma Mia! / La La Land / Grease
        Jaws / Piranha / The Meg
        The Avengers / Justice League / X-Men
        Oppenheimer / Barbie / Napoleon
        The Oscars / Golden Globes / BAFTAs
        Netflix / HBO Max / Disney+
        Spoiler / Trailer / Alternate ending
        3D glasses / IMAX / Drive-in
        The news / Game show / Reality show
        Who Wants to Be a Millionaire? / Jeopardy! / Wheel of Fortune
        The Office / Parks and Recreation / Brooklyn Nine-Nine
        """
    ),
    WordPacks.pack(
        "famous",
        "Famous people",
        "🌟",
        """
        Albert Einstein / Isaac Newton / Stephen Hawking
        Leonardo da Vinci / Michelangelo / Picasso
        Shakira / Rosalía / Beyoncé
        Rafael Nadal / Roger Federer / Novak Djokovic
        Michael Jackson / Elvis Presley / Freddie Mercury
        Cleopatra / Julius Caesar / Napoleon Bonaparte
        Christopher Columbus / Magellan / Marco Polo
        Mozart / Beethoven / Bach
        Frida Kahlo / Salvador Dalí / Van Gogh
        Marilyn Monroe / Audrey Hepburn / Madonna
        Elon Musk / Bill Gates / Steve Jobs
        Taylor Swift / Ariana Grande / Lady Gaga
        Bad Bunny / J Balvin / Daddy Yankee
        MrBeast / PewDiePie / Markiplier
        Lewis Hamilton / Max Verstappen / Fernando Alonso
        Michael Jordan / LeBron James / Kobe Bryant
        William Shakespeare / Charles Dickens / Jane Austen
        Tom Hanks / Meryl Streep / Denzel Washington
        Leonardo DiCaprio / Brad Pitt / Johnny Depp
        Usain Bolt / Michael Phelps / Simone Biles
        Gandhi / Martin Luther King / Nelson Mandela
        Marie Curie / Ada Lovelace / Charles Darwin
        Walt Disney / Steven Spielberg / Tim Burton
        Charlie Chaplin / Mr. Bean / Buster Keaton
        Queen Elizabeth II / Princess Diana / King Charles III
        David Bowie / Prince / Mick Jagger
        Neil Armstrong / Yuri Gagarin / Buzz Aldrin
        Kim Kardashian / Paris Hilton / Kylie Jenner
        Gordon Ramsay / Jamie Oliver / Ferran Adrià
        Dua Lipa / Billie Eilish / Rihanna
        Mark Zuckerberg / Jeff Bezos / Warren Buffett
        """
    ),
    WordPacks.pack(
        "characters",
        "Characters",
        "🧙",
        """
        Mickey Mouse / Donald Duck / Goofy
        Super Mario / Luigi / Sonic
        Pikachu / Jigglypuff / Stitch
        Sherlock Holmes / Hercule Poirot / Inspector Gadget
        Dracula / Frankenstein / Werewolf
        Little Red Riding Hood / Snow White / Cinderella
        Peter Pan / Pinocchio / Alice
        SpongeBob / Patrick / Squidward
        Homer Simpson / Peter Griffin / Fred Flintstone
        Darth Vader / Voldemort / Sauron
        Yoda / Gandalf / Dumbledore
        Joker / Harley Quinn / The Penguin
        Wonder Woman / Captain Marvel / Supergirl
        Iron Man / Captain America / Thor
        Hulk / Shrek / The Grinch
        Asterix / Obelix / Tintin
        Winnie the Pooh / Paddington / Peppa Pig
        Garfield / Snoopy / Scooby-Doo
        Don Quixote / Sancho Panza / Robinson Crusoe
        Robin Hood / Zorro / The Lone Ranger
        Santa Claus / Easter Bunny / Tooth Fairy
        Sleeping Beauty / Rapunzel / The Little Mermaid
        Doraemon / Shin-chan / Heidi
        Goku / Naruto / Luffy
        Mowgli / Tarzan / Simba
        Barbie / Ken / Polly Pocket
        Minions / Smurfs / Teletubbies
        Lara Croft / Indiana Jones / Nathan Drake
        Pac-Man / Donkey Kong / Kirby
        James Bond / Ethan Hunt / Jason Bourne
        Captain Hook / Jack Sparrow / Blackbeard
        """
    ),
    WordPacks.pack(
        "music",
        "Music",
        "🎤",
        """
        Guitar / Bass / Ukulele
        Piano / Keyboard / Organ
        Drums / Cajón / Bongos
        Violin / Cello / Double bass
        Trumpet / Saxophone / Trombone
        Flute / Clarinet / Bagpipes
        Rock / Heavy metal / Punk
        Reggaeton / Trap / Dembow
        Flamenco / Tango / Rumba
        Pop / Indie / K-pop
        Jazz / Blues / Soul
        Rap / Hip hop / Freestyle
        Classical music / Opera / Musical
        Concert / Festival / Tour
        Karaoke / Open mic / SingStar
        Microphone / Speaker / Headphones
        DJ / Music producer / Sound engineer
        Choir / Orchestra / Marching band
        Vinyl / CD / Cassette
        Eurovision / The Voice / American Idol
        The Beatles / The Rolling Stones / Queen
        Spotify / YouTube Music / Radio
        Christmas carol / National anthem / Lullaby
        Chorus / Verse / Guitar solo
        Dancing / Singing / Humming
        Country / Folk / Bluegrass
        Mariachi / Barbershop quartet / Brass band
        Reggae / Ska / Calypso
        Music video / Live show / Playlist
        Metronome / Sheet music / Music stand
        Gold record / Grammy / Number one
        """
    ),
    WordPacks.pack(
        "brands",
        "Brands",
        "👟",
        """
        Coca-Cola / Pepsi / Fanta
        Nike / Adidas / Puma
        McDonald's / Burger King / KFC
        Apple / Samsung / Xiaomi
        Zara / H&M / Primark
        IKEA / Home Depot / Costco
        Lidl / Aldi / Tesco
        Google / Bing / Yahoo
        Amazon / AliExpress / eBay
        WhatsApp / Telegram / Signal
        Instagram / TikTok / Snapchat
        YouTube / Twitch / Vimeo
        PlayStation / Xbox / Nintendo
        LEGO / Playmobil / Hot Wheels
        Ferrari / Lamborghini / Porsche
        Toyota / Ford / Honda
        Starbucks / Nespresso / Dunkin'
        Nutella / Kinder / Ferrero Rocher
        Chupa Chups / Haribo / M&M's
        Rolex / Casio / Swatch
        Ray-Ban / Oakley / Hawkers
        Visa / Mastercard / PayPal
        Red Bull / Monster / Gatorade
        Disney / Pixar / DreamWorks
        Uber / Lyft / BlaBlaCar
        Pringles / Doritos / Lay's
        Converse / Vans / New Balance
        Gucci / Prada / Louis Vuitton
        Microsoft / IBM / Intel
        Tesla / BMW / Mercedes
        """
    ),
    WordPacks.pack(
        "body",
        "Body & health",
        "❤️",
        """
        Heart / Lungs / Liver
        Brain / Neuron / Skull
        Eye / Ear / Nose
        Mouth / Lips / Tongue
        Tooth / Molar / Gums
        Hand / Foot / Wrist
        Knee / Elbow / Ankle
        Hair / Beard / Eyebrow
        Fingernail / Eyelash / Freckle
        Bone / Muscle / Tendon
        Blood / Sweat / Tears
        Cold / Flu / Allergy
        Fever / Chills / Headache
        Vaccine / Injection / Blood test
        Band-Aid / Bandage / Cast
        Cough syrup / Pill / Ointment
        Emergency room / Ambulance / Operating room
        Hiccups / Sneeze / Yawn
        Tickles / Cramp / Itch
        Diet / Fasting / Protein shake
        Sleep / Dream / Snore
        Belly button / Belly / Waist
        Skeleton / Spine / Ribs
        Stethoscope / Thermometer / Blood pressure monitor
        Glasses / Contact lenses / Hearing aid
        Breathing / Sighing / Panting
        Fingerprint / DNA / Blood type
        Pulse / Blood pressure / Heartbeat
        Wound / Bruise / Scar
        Braces / Retainer / Filling
        """
    ),
    WordPacks.pack(
        "school",
        "School",
        "🎒",
        """
        Pencil / Pen / Marker
        Eraser / Pencil sharpener / Correction fluid
        Notebook / Notepad / Planner
        Backpack / Pencil case / Folder
        Blackboard / Chalk / Projector
        Recess / Lunchtime / Playground
        Exam / Pop quiz / Group project
        Homework / Notes / Summary
        Maths / Physics / Chemistry
        History / Geography / Philosophy
        English / Spanish / French
        P.E. / Art / Technology
        Principal / Vice principal / Janitor
        School trip / End-of-year trip / Summer camp
        Holidays / Long weekend / Weekend
        Report card / Grades / Fail
        Cheat sheet / Copying / Earpiece
        Calculator / Ruler / Compass
        Dictionary / Atlas / Encyclopedia
        Parent-teacher meeting / Tutoring / Staff meeting
        School uniform / Lab coat / Tracksuit
        Prom / Graduation party / Open day
        Desk / Chair / Locker
        Map / Globe / Satellite photo
        New kid / Class rep / Teacher's pet
        Computer lab / Science lab / Gym hall
        Multiplication / Division / Square root
        Dictation / Essay / Book report
        Textbook / Required reading / Photocopy
        Periodic table / Times table / Solar system
        """
    ),
    WordPacks.pack(
        "fantasy",
        "Fantasy",
        "🐉",
        """
        Dragon / Dinosaur / Sea serpent
        Unicorn / Pegasus / Centaur
        Witch / Fairy / Sorceress
        Magic wand / Flying broom / Cauldron
        Goblin / Gnome / Elf
        Haunted castle / Haunted house / Labyrinth
        Mermaid / Merman / Nymph
        Vampire / Zombie / Mummy
        Ghost / Spirit / Poltergeist
        Troll / Ogre / Giant
        Potion / Spell / Curse
        Magic sword / Shield / Armor
        Knight / Princess / King
        Phoenix / Griffin / Chimera
        Flying carpet / Magic lamp / Genie
        Crystal ball / Tarot cards / Runes
        Portal / Teleportation / Time travel
        Superpowers / Invisibility / Telepathy
        Kraken / Leviathan / Loch Ness Monster
        Yeti / Bigfoot / Chupacabra
        Treasure chest / Treasure map / Treasure island
        Prophecy / Chosen one / Legend
        Kingdom / Empire / Village
        Pirate / Privateer / Viking
        Dragon egg / Golden egg / Philosopher's stone
        Angel / Demon / Cherub
        Alien / Robot / Mutant
        Minotaur / Cyclops / Medusa
        Enchanted forest / Secret garden / Dragon's cave
        Elixir / Nectar / Ambrosia
        """
    ),
    WordPacks.pack(
        "tech",
        "Technology",
        "📱",
        """
        Smartphone / Tablet / Smartwatch
        Computer / Laptop / Games console
        Wi-Fi / Bluetooth / Mobile data
        Password / PIN / Face ID
        Selfie / Photo / Video
        Emoji / Sticker / GIF
        Drone / Robot / Artificial intelligence
        Printer / Scanner / Photocopier
        Charger / Power bank / Plug
        Mouse / Keyboard / Touchpad
        Screen / Monitor / Projector
        Video call / Phone call / Voice message
        Email / Text message / Fax
        The cloud / USB stick / Hard drive
        Virus / Hacker / Spam
        Update / Restart / Blue screen
        Influencer / Streamer / YouTuber
        Virtual reality / Augmented reality / Hologram
        Alexa / Siri / Google Assistant
        GPS / Map / Compass
        QR code / Barcode / NFC
        App / Website / Video game
        Screenshot / Screen recording / Copy and paste
        Like / Comment / Share
        Hashtag / At sign / Link
        Television / Radio / Record player
        Camera / Webcam / GoPro
        Electric scooter / E-bike / Hoverboard
        Airplane mode / Silent mode / Do not disturb
        Router / Antenna / Satellite
        """
    ),
    WordPacks.pack(
        "transport",
        "Transport",
        "🚗",
        """
        Car / Motorbike / Quad bike
        Bus / Coach / Tram
        Train / Subway / High-speed train
        Plane / Helicopter / Light aircraft
        Boat / Sailboat / Yacht
        Bicycle / Unicycle / Tricycle
        Taxi / Uber / Limousine
        Truck / Van / Tow truck
        Submarine / Speedboat / Kayak
        Rocket / Spaceship / UFO
        Hot-air balloon / Airship / Hang glider
        Fire engine / Ambulance / Police car
        Cable car / Chairlift / Funicular
        Caravan / Motorhome / Tent
        Tractor / Combine harvester / Excavator
        Shopping cart / Pram / Wheelchair
        Ferry / Cruise ship / Catamaran
        Carriage / Cart / Sleigh
        Petrol / Diesel / Electric
        Traffic light / Zebra crossing / Roundabout
        Motorway / Road / Toll booth
        Car park / Garage / Parking meter
        Driving licence / Passport / Ticket
        Suitcase / Carry-on / Toiletry bag
        Seat belt / Helmet / Airbag
        Steering wheel / Brake / Clutch
        Flat tire / Breakdown / Tow truck
        Bus station / Bus stop / Platform
        Gondola / Canoe / Raft
        Jet ski / Banana boat / Pedal boat
        """
    ),
    WordPacks.pack(
        "fashion",
        "Fashion & clothes",
        "👗",
        """
        T-shirt / Shirt / Polo shirt
        Trousers / Jeans / Leggings
        Skirt / Dress / Sarong
        Jacket / Coat / Hoodie
        Sneakers / Shoes / Boots
        Sandals / Flip-flops / Espadrilles
        Cap / Hat / Beanie
        Scarf / Bandana / Tie
        Gloves / Mittens / Socks
        Pajamas / Bathrobe / Dressing gown
        Bikini / Swimsuit / Wetsuit
        Sunglasses / Visor / Headband
        Necklace / Bracelet / Earrings
        Ring / Wedding ring / Signet ring
        Handbag / Bum bag / Wallet
        Belt / Suspenders / Bow tie
        Suit / Tuxedo / Morning suit
        Wedding dress / Flamenco dress / Costume
        High heels / Platforms / Ballet flats
        Boxers / Knickers / Bra
        Tights / Leg warmers / Stockings
        Parka / Windbreaker / Puffer jacket
        Catwalk / Fashion show / Fitting room
        Sales / Black Friday / Outlet
        Makeup / Lipstick / Mascara
        Perfume / Cologne / Deodorant
        Hairstyle / Ponytail / Braid
        Tattoo / Piercing / Henna
        Button / Zipper / Velcro
        Wool / Cotton / Silk
        """
    ),
    WordPacks.pack(
        "summer",
        "Summer",
        "🏝️",
        """
        Beach towel / Parasol / Beach mat
        Sunscreen / After-sun / Tanning oil
        Beach bar / Terrace / Picnic area
        Sandcastle / Bucket and spade / Seashell
        Rubber ring / Lilo / Armbands
        Wave / Tide / Current
        Red flag / Yellow flag / Jellyfish warning
        Suntan / Sunburn / Heatstroke
        Fan / Air conditioning / Hand fan
        Diving mask / Flippers / Snorkel
        Village festival / Street party / Fair
        Summer camp / Scout camp / Sports camp
        Mosquito / Fly / Horsefly
        Summer storm / Heatwave / Drought
        Cannonball / Belly flop / Dive
        Iced tea / Horchata / Strawberry milkshake
        Sun lounger / Hammock / Deck chair
        Sunset / Sunrise / Starry night
        Paddling pool / Public pool / Spa
        Barbecue / Picnic / Campfire
        Shooting star / Full moon / Meteor shower
        Postcard / Souvenir / Fridge magnet
        All-inclusive wristband / Buffet / Beach resort
        Water slide / Wave pool / Lazy river
        Backpacking / Interrail / Road trip
        Cool box / Ice pack / Water bottle
        Beach ball / Frisbee / Beach tennis
        Promenade / Pier / Marina
        Coconut water / Pineapple juice / Slushie
        Kite / Pinwheel / Soap bubbles
        """
    ),
    WordPacks.pack(
        "games",
        "Games",
        "🎲",
        """
        Chess / Checkers / Backgammon
        Ludo / Snakes and ladders / Sorry!
        Monopoly / Catan / Risk
        Poker / Blackjack / Rummy
        Go Fish / Old Maid / Snap
        Uno / Dobble / Jungle Speed
        Trivial Pursuit / Quiz / Riddles
        Pictionary / Taboo / Charades
        Hide and seek / Tag / Cops and robbers
        Jump rope / Hopscotch / Elastics
        Marbles / Spinning top / Yo-yo
        Jigsaw puzzle / Sudoku / Crossword
        Word search / Hangman / Tic-tac-toe
        Jenga / Dominoes / Pick-up sticks
        Twister / Musical chairs / Telephone
        Minecraft / Roblox / Fortnite
        Tetris / Candy Crush / Minesweeper
        FIFA / NBA 2K / Rocket League
        Mario Kart / Crash Bandicoot / Need for Speed
        Pokémon GO / Geocaching / Treasure hunt
        Among Us / Werewolf / Mafia
        Escape room / Scavenger hunt / Cluedo
        Bingo / Lottery / Scratch card
        Pokémon cards / Sticker album / Pogs
        Swing / Slide / Seesaw
        Piñata / Balloons / Confetti
        Role-playing game / Dungeons & Dragons / Warhammer
        Scrabble / Words With Friends / Wordle
        Truth or dare / Guess Who / Simon says
        Rubik's Cube / Fidget spinner / Slime
        """
    ),
    WordPacks.pack(
        "parties",
        "Celebrations",
        "🎉",
        """
        Christmas / New Year / Thanksgiving
        Halloween / Day of the Dead / Carnival
        New Year's Eve / Countdown party / Party hats
        Christmas tree / Nativity scene / Advent calendar
        Gingerbread house / Panettone / Christmas pudding
        Running of the bulls / La Tomatina / Fallas
        Easter egg hunt / Easter basket / Hot cross buns
        Valentine's Day / Mother's Day / Father's Day
        Wedding / Baptism / Bar mitzvah
        Bachelor party / Surprise party / Baby shower
        Trick or treat / Haunted house / Costume party
        Fireworks / Firecrackers / Sparklers
        Parade / Procession / Float
        Present / Surprise / Parcel
        Toast / Speech / First dance
        Mistletoe / Holly / Poinsettia
        Blowing out candles / Making a wish / Singing happy birthday
        Secret Santa / Christmas hamper / Christmas cracker
        Bonfire Night / Midsummer / Fourth of July
        Venice Carnival / St. Patrick's Day / Rio Carnival
        Chinese New Year / Diwali / Holi
        Cobweb / Coffin / Gravestone
        Wrapping paper / Bow / Greeting card
        Mask / Masquerade / Zombie makeup
        Christmas dinner / Thanksgiving dinner / Sunday roast
        Times Square ball drop / Big Ben chimes / Countdown
        Letter to Santa / Letter to the Three Kings / Wish list
        Santa hat / Reindeer antlers / Party hat
        Reindeer / Moose / Deer
        Pumpkin carving / Candy corn / Toffee apple
        """
    )
)
