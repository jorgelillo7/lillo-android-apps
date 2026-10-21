package com.jorgelillo.whoslying.domain

/** Spanish built-in packs. Each line: word / similar word / similar word. */
internal val spanishPacks = listOf(
    WordPacks.pack(
        "food",
        "Comida",
        "🍕",
        """
        Pizza / Hamburguesa / Kebab
        Paella / Risotto / Fideuá
        Tortilla de patatas / Revuelto / Quiche
        Churros / Porras / Rosquillas
        Croquetas / Empanadillas / Buñuelos
        Gazpacho / Salmorejo / Crema de verduras
        Sushi / Ramen / Gyozas
        Helado / Granizado / Polo
        Chocolate / Caramelo / Turrón
        Café / Té / Infusión
        Naranja / Mandarina / Pomelo
        Fresa / Cereza / Frambuesa
        Jamón / Chorizo / Lomo
        Queso / Yogur / Mantequilla
        Pan / Tostada / Bollo
        Patatas fritas / Nachos / Gusanitos
        Lentejas / Garbanzos / Alubias
        Tarta / Flan / Natillas
        Palomitas / Pipas / Cacahuetes
        Aceitunas / Pepinillos / Banderillas
        Huevo frito / Huevo cocido / Tortilla francesa
        Macarrones / Espaguetis / Lasaña
        Plátano / Mango / Piña
        Sandía / Melón / Papaya
        Pollo asado / Pavo / Conejo
        Calamares / Gambas / Pulpo
        Bocadillo / Sándwich / Wrap
        Ensalada / Pisto / Verduras a la plancha
        Donut / Cruasán / Magdalena
        Cereales / Avena / Muesli
        Limonada / Zumo de naranja / Batido
        """
    ),
    WordPacks.pack(
        "places",
        "Mundo y lugares",
        "🗺️",
        """
        Playa / Piscina / Lago
        Cine / Teatro / Auditorio
        Hospital / Centro de salud / Farmacia
        Aeropuerto / Estación de tren / Puerto
        Supermercado / Mercado / Tienda de barrio
        Biblioteca / Librería / Archivo
        Gimnasio / Polideportivo / Piscina cubierta
        Discoteca / Bar / Karaoke
        Zoo / Acuario / Granja
        Museo / Galería de arte / Castillo
        Iglesia / Catedral / Monasterio
        Montaña / Colina / Volcán
        Desierto / Sabana / Estepa
        Selva / Bosque / Manglar
        Parque de atracciones / Feria / Parque acuático
        Restaurante / Cafetería / Food truck
        Hotel / Albergue / Camping
        París / Londres / Roma
        Nueva York / Los Ángeles / Chicago
        Japón / China / Corea del Sur
        Egipto / Marruecos / Túnez
        Brasil / Argentina / México
        Barcelona / Madrid / Valencia
        Antártida / Polo Norte / Groenlandia
        Isla desierta / Barco hundido / Faro
        Cárcel / Comisaría / Juzgado
        Universidad / Instituto / Academia
        Peluquería / Barbería / Centro de estética
        Ayuntamiento / Oficina de correos / Banco
        Estadio / Pabellón / Circuito
        Gasolinera / Taller / Lavadero de coches
        """
    ),
    WordPacks.pack(
        "animals",
        "Animales y naturaleza",
        "🦁",
        """
        León / Tigre / Leopardo
        Perro / Lobo / Zorro
        Gato / Lince / Hurón
        Delfín / Ballena / Tiburón
        Elefante / Rinoceronte / Hipopótamo
        Jirafa / Cebra / Camello
        Pingüino / Foca / Morsa
        Águila / Halcón / Buitre
        Loro / Tucán / Periquito
        Serpiente / Lagarto / Cocodrilo
        Rana / Sapo / Salamandra
        Mariposa / Polilla / Libélula
        Abeja / Avispa / Mosca
        Hormiga / Termita / Cucaracha
        Caballo / Burro / Poni
        Vaca / Toro / Búfalo
        Oveja / Cabra / Llama
        Cerdo / Jabalí / Tejón
        Gallina / Pato / Ganso
        Oso / Panda / Koala
        Mono / Gorila / Chimpancé
        Canguro / Liebre / Ardilla
        Pulpo / Calamar / Medusa
        Tortuga / Caracol / Armadillo
        Búho / Murciélago / Cuervo
        Girasol / Margarita / Tulipán
        Rosa / Clavel / Orquídea
        Cactus / Aloe vera / Palmera
        Arcoíris / Aurora boreal / Eclipse
        Tormenta / Tornado / Huracán
        Cascada / Río / Fuente
        Roble / Pino / Sauce
        """
    ),
    WordPacks.pack(
        "home",
        "Vida cotidiana",
        "☕",
        """
        Sofá / Sillón / Puf
        Cama / Litera / Hamaca
        Nevera / Congelador / Despensa
        Microondas / Horno / Tostadora
        Lavadora / Secadora / Lavavajillas
        Ducha / Bañera / Lavabo
        Espejo / Ventana / Cuadro
        Paraguas / Chubasquero / Sombrilla
        Llaves / Candado / Cerradura
        Mando a distancia / Teclado / Interruptor
        Almohada / Cojín / Peluche
        Manta / Edredón / Toalla
        Cepillo de dientes / Peine / Hilo dental
        Despertador / Reloj / Temporizador
        Escoba / Fregona / Aspiradora
        Vela / Lámpara / Linterna
        Planta / Florero / Maceta
        Basura / Reciclaje / Papelera
        Siesta / Madrugar / Insomnio
        Hacer la compra / Poner la lavadora / Cocinar
        Atasco / Cola del súper / Sala de espera
        Lunes / Domingo / Viernes
        Cumpleaños / Aniversario / Graduación
        Mudanza / Reforma / Limpieza general
        Vecino / Portero / Casero
        Buzón / Timbre / Telefonillo
        Cortina / Persiana / Estor
        Tendedero / Plancha / Percha
        Taza / Vaso / Termo
        Tenedor / Cuchara / Palillos
        """
    ),
    WordPacks.pack(
        "jobs",
        "Profesiones",
        "👩‍🚒",
        """
        Bombero / Policía / Socorrista
        Médico / Enfermero / Veterinario
        Profesor / Monitor / Entrenador
        Cocinero / Camarero / Panadero
        Piloto / Azafato / Controlador aéreo
        Astronauta / Astrónomo / Científico
        Abogado / Juez / Notario
        Peluquero / Maquillador / Estilista
        Fontanero / Electricista / Albañil
        Periodista / Presentador / Locutor
        Fotógrafo / Pintor / Diseñador
        Actor / Cantante / Bailarín
        Arquitecto / Ingeniero / Aparejador
        Granjero / Pescador / Jardinero
        Cartero / Repartidor / Mensajero
        Dentista / Óptico / Fisioterapeuta
        Programador / Informático / Youtuber
        Detective / Espía / Guardaespaldas
        Mago / Payaso / Malabarista
        Taxista / Camionero / Conductor de autobús
        Carpintero / Herrero / Escultor
        Banquero / Contable / Cajero
        Futbolista / Árbitro / Portero
        Militar / Marinero / Capitán
        Farmacéutico / Químico / Biólogo
        Psicólogo / Psiquiatra / Coach
        Mecánico / Chapista / Gasolinero
        Pastelero / Heladero / Chocolatero
        Guía turístico / Recepcionista / Agente de viajes
        Dependiente / Cajero / Reponedor
        """
    ),
    WordPacks.pack(
        "sports",
        "Deportes",
        "🏅",
        """
        Rugby / Fútbol americano / Lacrosse
        Baloncesto / Balonmano / Voleibol
        Tenis / Pádel / Bádminton
        Natación / Waterpolo / Saltos de trampolín
        Ciclismo / Mountain bike / Spinning
        Esquí / Snowboard / Patinaje sobre hielo
        Surf / Windsurf / Paddle surf
        Boxeo / Kárate / Judo
        Golf / Minigolf / Petanca
        Atletismo / Maratón / Triatlón
        Gimnasia / Yoga / Pilates
        Escalada / Senderismo / Rápel
        Fórmula 1 / MotoGP / Rally
        Béisbol / Críquet / Softball
        Hockey sobre hielo / Curling / Patinaje artístico
        Equitación / Polo / Rodeo
        Tiro con arco / Dardos / Tiro olímpico
        Esgrima / Kendo / Lucha libre
        Remo / Piragüismo / Vela
        Skate / Patines / Patinete
        Ping-pong / Squash / Frontón
        Paracaidismo / Puenting / Parapente
        Juegos Olímpicos / Mundial / Super Bowl
        Medalla de oro / Trofeo / Podio
        Dorsal / Silbato / Cronómetro
        Calentamiento / Estiramientos / Agujetas
        Buceo / Snorkel / Apnea
        Bolos / Billar / Futbolín
        Zumba / Aeróbic / Crossfit
        Sumo / Pulso / Tira y afloja
        """
    ),
    WordPacks.pack(
        "football",
        "Fútbol",
        "⚽",
        """
        Gol / Penalti / Falta
        Portero / Defensa / Delantero
        Tarjeta roja / Tarjeta amarilla / Expulsión
        Fuera de juego / Mano / VAR
        Córner / Saque de banda / Saque de puerta
        Real Madrid / Barcelona / Atlético de Madrid
        Messi / Cristiano Ronaldo / Neymar
        Mundial / Eurocopa / Copa América
        Champions / Europa League / La Liga
        Balón de Oro / Bota de Oro / Trofeo Zamora
        Entrenador / Presidente del club / Representante
        Fichaje / Cesión / Traspaso
        Derbi / El Clásico / Final de Copa
        Prórroga / Tanda de penaltis / Descanso
        Chilena / Tijera / Taconazo
        Camiseta del equipo / Bufanda / Banderín
        Afición / Ultras / Peña
        Árbitro / Linier / Cuarto árbitro
        Brazalete de capitán / Dorsal / Espinilleras
        Hat-trick / Doblete / Gol en propia puerta
        Vestuario / Banquillo / Túnel de vestuarios
        Alineación / Táctica / Pizarra
        Pichichi / Máximo asistente / Mejor jugador del partido
        Ascenso / Descenso / Playoff
        Fútbol sala / Fútbol playa / Futbolín
        Pelé / Maradona / Zidane
        Iniesta / Xavi / Busquets
        Lamine Yamal / Pedri / Gavi
        Mbappé / Haaland / Vinícius
        Selección española / Selección argentina / Selección brasileña
        """
    ),
    WordPacks.pack(
        "movies",
        "Cine y series",
        "🎬",
        """
        Titanic / Avatar / Pearl Harbor
        Harry Potter / El Señor de los Anillos / Las Crónicas de Narnia
        Star Wars / Star Trek / Guardianes de la Galaxia
        Spider-Man / Batman / Superman
        Frozen / Enredados / Vaiana
        El Rey León / El Libro de la Selva / Tarzán
        Toy Story / Cars / Monstruos S.A.
        Shrek / Madagascar / Kung Fu Panda
        Jurassic Park / King Kong / Godzilla
        Piratas del Caribe / Indiana Jones / La Momia
        Los Simpson / Padre de familia / Futurama
        Friends / Cómo conocí a vuestra madre / The Big Bang Theory
        La casa de papel / Élite / Vis a vis
        Stranger Things / Dark / Los Goonies
        Juego de Tronos / La Casa del Dragón / The Witcher
        Breaking Bad / Narcos / Better Call Saul
        El juego del calamar / Alice in Borderland / Los juegos del hambre
        A todo gas / 60 segundos / Baby Driver
        Matrix / Origen / Terminator
        Regreso al futuro / Interstellar / Atrapado en el tiempo
        Mamma Mia! / La La Land / Grease
        Tiburón / Pirañas / Megalodón
        Los Vengadores / La Liga de la Justicia / X-Men
        Oppenheimer / Barbie / Napoleón
        Los Óscar / Los Goya / Los Globos de Oro
        Netflix / HBO Max / Disney+
        Spoiler / Tráiler / Final alternativo
        Gafas 3D / IMAX / Autocine
        Telediario / Concurso / Reality show
        Pasapalabra / Saber y ganar / ¿Quién quiere ser millonario?
        La que se avecina / Aquí no hay quien viva / Los Serrano
        """
    ),
    WordPacks.pack(
        "famous",
        "Gente famosa",
        "🌟",
        """
        Albert Einstein / Isaac Newton / Stephen Hawking
        Leonardo da Vinci / Miguel Ángel / Picasso
        Shakira / Rosalía / Beyoncé
        Rafa Nadal / Roger Federer / Novak Djokovic
        Michael Jackson / Elvis Presley / Freddie Mercury
        Cleopatra / Julio César / Napoleón
        Cristóbal Colón / Magallanes / Marco Polo
        Mozart / Beethoven / Bach
        Frida Kahlo / Salvador Dalí / Van Gogh
        Marilyn Monroe / Audrey Hepburn / Madonna
        Elon Musk / Bill Gates / Steve Jobs
        Taylor Swift / Ariana Grande / Lady Gaga
        Bad Bunny / J Balvin / Daddy Yankee
        Ibai / El Rubius / AuronPlay
        Fernando Alonso / Carlos Sainz / Lewis Hamilton
        Pau Gasol / Michael Jordan / LeBron James
        Cervantes / Shakespeare / Lope de Vega
        Antonio Banderas / Penélope Cruz / Javier Bardem
        Leonardo DiCaprio / Brad Pitt / Johnny Depp
        Usain Bolt / Michael Phelps / Simone Biles
        Gandhi / Martin Luther King / Nelson Mandela
        Marie Curie / Ada Lovelace / Charles Darwin
        Walt Disney / Steven Spielberg / Tim Burton
        Charles Chaplin / Mr. Bean / Cantinflas
        Isabel II / Lady Di / Carlos III
        David Bowie / Prince / Mick Jagger
        Neil Armstrong / Yuri Gagarin / Pedro Duque
        Kim Kardashian / Paris Hilton / Georgina Rodríguez
        Karlos Arguiñano / Alberto Chicote / Ferran Adrià
        Dua Lipa / Billie Eilish / Rihanna
        Mark Zuckerberg / Jeff Bezos / Amancio Ortega
        """
    ),
    WordPacks.pack(
        "characters",
        "Personajes",
        "🧙",
        """
        Mickey Mouse / Pato Donald / Goofy
        Super Mario / Luigi / Sonic
        Pikachu / Jigglypuff / Stitch
        Sherlock Holmes / Hércules Poirot / Inspector Gadget
        Drácula / Frankenstein / Hombre lobo
        Caperucita Roja / Blancanieves / Cenicienta
        Peter Pan / Pinocho / Alicia
        Bob Esponja / Patricio / Calamardo
        Homer Simpson / Peter Griffin / Pedro Picapiedra
        Darth Vader / Voldemort / Sauron
        Yoda / Gandalf / Dumbledore
        Joker / Harley Quinn / Pingüino
        Wonder Woman / Capitana Marvel / Supergirl
        Iron Man / Capitán América / Thor
        Hulk / Shrek / Grinch
        Astérix / Obélix / Tintín
        Mortadelo / Filemón / Superlópez
        Garfield / Snoopy / Scooby-Doo
        Don Quijote / Sancho Panza / Lazarillo de Tormes
        Robin Hood / El Zorro / El Llanero Solitario
        Papá Noel / Reyes Magos / Ratoncito Pérez
        La Bella Durmiente / Rapunzel / La Sirenita
        Doraemon / Shin Chan / Heidi
        Goku / Naruto / Luffy
        Mowgli / Tarzán / Simba
        Barbie / Ken / Polly Pocket
        Minions / Pitufos / Teletubbies
        Lara Croft / Indiana Jones / Nathan Drake
        Pac-Man / Donkey Kong / Kirby
        James Bond / Ethan Hunt / Jason Bourne
        Capitán Garfio / Jack Sparrow / Barbanegra
        """
    ),
    WordPacks.pack(
        "music",
        "Música",
        "🎤",
        """
        Guitarra / Bajo / Ukelele
        Piano / Teclado / Órgano
        Batería / Cajón flamenco / Bongos
        Violín / Violonchelo / Contrabajo
        Trompeta / Saxofón / Trombón
        Flauta / Clarinete / Gaita
        Rock / Heavy metal / Punk
        Reguetón / Trap / Dembow
        Flamenco / Sevillanas / Rumba
        Pop / Indie / K-pop
        Jazz / Blues / Soul
        Rap / Hip hop / Freestyle
        Música clásica / Ópera / Zarzuela
        Concierto / Festival / Gira
        Karaoke / Micrófono abierto / SingStar
        Micrófono / Altavoz / Auriculares
        DJ / Productor / Técnico de sonido
        Coro / Orquesta / Banda de música
        Vinilo / CD / Casete
        Eurovisión / Operación Triunfo / La Voz
        The Beatles / The Rolling Stones / Queen
        Spotify / YouTube Music / Radio
        Villancico / Himno / Canción de cuna
        Estribillo / Estrofa / Solo de guitarra
        Bailar / Cantar / Tararear
        Mariachi / Tuna / Charanga
        Reggae / Ska / Calypso
        Videoclip / Directo / Playlist
        Metrónomo / Partitura / Atril
        Disco de oro / Grammy / Número uno
        """
    ),
    WordPacks.pack(
        "brands",
        "Marcas",
        "👟",
        """
        Coca-Cola / Pepsi / Fanta
        Nike / Adidas / Puma
        McDonald's / Burger King / KFC
        Apple / Samsung / Xiaomi
        Zara / H&M / Primark
        Ikea / Leroy Merlin / El Corte Inglés
        Mercadona / Lidl / Carrefour
        Google / Bing / Yahoo
        Amazon / AliExpress / eBay
        WhatsApp / Telegram / Signal
        Instagram / TikTok / Snapchat
        YouTube / Twitch / Vimeo
        PlayStation / Xbox / Nintendo
        Lego / Playmobil / Hot Wheels
        Ferrari / Lamborghini / Porsche
        Toyota / Seat / Renault
        Starbucks / Nespresso / Dunkin'
        Nutella / Nocilla / Cola Cao
        Chupa Chups / Haribo / Kinder
        Rolex / Casio / Swatch
        Ray-Ban / Oakley / Hawkers
        Visa / Mastercard / PayPal
        Red Bull / Monster / Aquarius
        Disney / Pixar / DreamWorks
        Uber / Cabify / BlaBlaCar
        Pringles / Doritos / Lay's
        Converse / Vans / New Balance
        Gucci / Prada / Louis Vuitton
        Microsoft / IBM / Intel
        Tesla / BMW / Mercedes
        """
    ),
    WordPacks.pack(
        "body",
        "Cuerpo y salud",
        "❤️",
        """
        Corazón / Pulmón / Hígado
        Cerebro / Neurona / Cráneo
        Ojo / Oreja / Nariz
        Boca / Labios / Lengua
        Diente / Muela / Encía
        Mano / Pie / Muñeca
        Rodilla / Codo / Tobillo
        Pelo / Barba / Ceja
        Uña / Pestaña / Lunar
        Hueso / Músculo / Tendón
        Sangre / Sudor / Lágrimas
        Resfriado / Gripe / Alergia
        Fiebre / Escalofríos / Dolor de cabeza
        Vacuna / Inyección / Análisis de sangre
        Tirita / Venda / Escayola
        Jarabe / Pastilla / Pomada
        Urgencias / Ambulancia / Quirófano
        Hipo / Estornudo / Bostezo
        Cosquillas / Calambre / Picor
        Dieta / Ayuno / Batido de proteínas
        Dormir / Soñar / Roncar
        Ombligo / Barriga / Cintura
        Esqueleto / Calavera / Columna vertebral
        Estetoscopio / Termómetro / Tensiómetro
        Gafas / Lentillas / Audífono
        Respirar / Suspirar / Jadear
        Huella dactilar / ADN / Grupo sanguíneo
        Pulso / Tensión / Latido
        Herida / Moratón / Cicatriz
        Ortodoncia / Brackets / Empaste
        """
    ),
    WordPacks.pack(
        "school",
        "Colegio",
        "🎒",
        """
        Lápiz / Bolígrafo / Rotulador
        Goma / Sacapuntas / Típex
        Cuaderno / Libreta / Agenda
        Mochila / Estuche / Carpeta
        Pizarra / Tiza / Proyector
        Recreo / Comedor / Patio
        Examen / Control sorpresa / Trabajo en grupo
        Deberes / Apuntes / Resumen
        Matemáticas / Física / Química
        Historia / Geografía / Filosofía
        Lengua / Inglés / Francés
        Educación física / Plástica / Tecnología
        Director / Jefe de estudios / Conserje
        Excursión / Viaje de fin de curso / Colonias
        Vacaciones / Puente / Fin de semana
        Notas / Boletín / Suspenso
        Chuleta / Copiar / Pinganillo
        Calculadora / Regla / Compás
        Diccionario / Atlas / Enciclopedia
        Tutoría / Reunión de padres / Claustro
        Uniforme / Bata / Chándal
        Fiesta de fin de curso / Graduación / Jornada de puertas abiertas
        Pupitre / Silla / Taquilla
        Mapa / Globo terráqueo / Foto de satélite
        Alumno nuevo / Delegado / Repetidor
        Sala de ordenadores / Laboratorio / Gimnasio
        Multiplicación / División / Raíz cuadrada
        Dictado / Redacción / Comentario de texto
        Libro de texto / Lectura obligatoria / Fotocopia
        Tabla periódica / Tabla de multiplicar / Sistema solar
        """
    ),
    WordPacks.pack(
        "fantasy",
        "Fantasía",
        "🐉",
        """
        Dragón / Dinosaurio / Serpiente marina
        Unicornio / Pegaso / Centauro
        Bruja / Hada / Hechicera
        Varita mágica / Escoba voladora / Caldero
        Duende / Gnomo / Elfo
        Castillo encantado / Casa encantada / Laberinto
        Sirena / Tritón / Ninfa
        Vampiro / Zombi / Momia
        Fantasma / Espíritu / Poltergeist
        Troll / Ogro / Gigante
        Poción / Hechizo / Maldición
        Espada mágica / Escudo / Armadura
        Caballero / Princesa / Rey
        Fénix / Grifo / Quimera
        Alfombra voladora / Lámpara maravillosa / Genio
        Bola de cristal / Cartas del tarot / Runas
        Portal / Teletransporte / Viaje en el tiempo
        Superpoderes / Invisibilidad / Telepatía
        Kraken / Leviatán / Monstruo del lago Ness
        Yeti / Pie Grande / Chupacabras
        Cofre del tesoro / Mapa del tesoro / Isla del tesoro
        Profecía / Elegido / Leyenda
        Reino / Imperio / Aldea
        Pirata / Corsario / Vikingo
        Huevo de dragón / Huevo de oro / Piedra filosofal
        Ángel / Demonio / Querubín
        Alienígena / Robot / Mutante
        Minotauro / Cíclope / Medusa
        Bosque encantado / Jardín secreto / Cueva del dragón
        Elixir / Néctar / Ambrosía
        """
    ),
    WordPacks.pack(
        "tech",
        "Tecnología",
        "📱",
        """
        Móvil / Tablet / Smartwatch
        Ordenador / Portátil / Consola
        Wifi / Bluetooth / Datos móviles
        Contraseña / PIN / Reconocimiento facial
        Selfie / Foto / Vídeo
        Emoji / Sticker / GIF
        Dron / Robot / Inteligencia artificial
        Impresora / Escáner / Fotocopiadora
        Cargador / Batería externa / Enchufe
        Ratón / Teclado / Touchpad
        Pantalla / Monitor / Proyector
        Videollamada / Llamada / Audio de WhatsApp
        Correo electrónico / SMS / Fax
        La nube / Pendrive / Disco duro
        Virus / Hacker / Spam
        Actualización / Reinicio / Pantallazo azul
        Influencer / Streamer / Youtuber
        Realidad virtual / Realidad aumentada / Holograma
        Alexa / Siri / Asistente de Google
        GPS / Mapa / Brújula
        Código QR / Código de barras / NFC
        App / Web / Videojuego
        Captura de pantalla / Grabación de pantalla / Copiar y pegar
        Like / Comentario / Compartir
        Hashtag / Arroba / Enlace
        Televisión / Radio / Tocadiscos
        Cámara / Webcam / GoPro
        Patinete eléctrico / Bici eléctrica / Hoverboard
        Modo avión / Modo silencio / No molestar
        Router / Antena / Satélite
        """
    ),
    WordPacks.pack(
        "transport",
        "Transportes",
        "🚗",
        """
        Coche / Moto / Quad
        Autobús / Autocar / Tranvía
        Tren / Metro / AVE
        Avión / Helicóptero / Avioneta
        Barco / Velero / Yate
        Bicicleta / Monociclo / Triciclo
        Taxi / Uber / Limusina
        Camión / Furgoneta / Grúa
        Submarino / Lancha / Kayak
        Cohete / Nave espacial / Ovni
        Globo aerostático / Dirigible / Ala delta
        Camión de bomberos / Ambulancia / Coche de policía
        Teleférico / Telesilla / Funicular
        Caravana / Autocaravana / Tienda de campaña
        Tractor / Cosechadora / Excavadora
        Carrito de la compra / Carrito de bebé / Silla de ruedas
        Ferry / Crucero / Catamarán
        Carruaje / Carro / Trineo
        Gasolina / Diésel / Eléctrico
        Semáforo / Paso de cebra / Rotonda
        Autopista / Carretera / Peaje
        Aparcamiento / Garaje / Zona azul
        Carnet de conducir / Pasaporte / Billete
        Maleta / Equipaje de mano / Neceser
        Cinturón de seguridad / Casco / Airbag
        Volante / Freno / Embrague
        Pinchazo / Avería / Grúa
        Estación de autobuses / Parada / Andén
        Góndola / Canoa / Balsa
        Moto de agua / Banana hinchable / Hidropedal
        """
    ),
    WordPacks.pack(
        "fashion",
        "Moda y ropa",
        "👗",
        """
        Camiseta / Camisa / Polo
        Pantalón / Vaqueros / Mallas
        Falda / Vestido / Pareo
        Chaqueta / Abrigo / Sudadera
        Zapatillas / Zapatos / Botas
        Sandalias / Chanclas / Alpargatas
        Gorra / Sombrero / Gorro
        Bufanda / Pañuelo / Corbata
        Guantes / Manoplas / Calcetines
        Pijama / Bata / Albornoz
        Bikini / Bañador / Traje de neopreno
        Gafas de sol / Visera / Diadema
        Collar / Pulsera / Pendientes
        Anillo / Alianza / Sortija
        Bolso / Riñonera / Cartera
        Cinturón / Tirantes / Pajarita
        Traje / Esmoquin / Chaqué
        Vestido de novia / Traje de flamenca / Disfraz
        Tacones / Plataformas / Bailarinas
        Calzoncillos / Bragas / Sujetador
        Medias / Leotardos / Calentadores
        Impermeable / Parka / Cortavientos
        Pasarela / Desfile / Probador
        Rebajas / Black Friday / Outlet
        Maquillaje / Pintalabios / Rímel
        Perfume / Colonia / Desodorante
        Peinado / Coleta / Trenza
        Tatuaje / Piercing / Henna
        Botón / Cremallera / Velcro
        Lana / Algodón / Seda
        """
    ),
    WordPacks.pack(
        "summer",
        "Verano",
        "🏝️",
        """
        Toalla de playa / Sombrilla / Esterilla
        Crema solar / Aftersun / Bronceador
        Chiringuito / Terraza / Merendero
        Castillo de arena / Pala y cubo / Concha
        Flotador / Colchoneta / Manguitos
        Ola / Marea / Corriente
        Bandera roja / Bandera amarilla / Medusas
        Moreno / Quemadura solar / Insolación
        Ventilador / Aire acondicionado / Abanico
        Gafas de bucear / Aletas / Tubo
        Fiestas del pueblo / Verbena / Feria
        Campamento / Colonias de verano / Campus deportivo
        Mosquito / Mosca / Tábano
        Tormenta de verano / Ola de calor / Sequía
        Chapuzón / Bomba / Salto de cabeza
        Té helado / Horchata / Batido de fresa
        Tumbona / Hamaca / Silla plegable
        Puesta de sol / Amanecer / Noche estrellada
        Piscina municipal / Piscina hinchable / Spa
        Barbacoa / Picnic / Merienda en el campo
        Estrella fugaz / Luna llena / Lluvia de estrellas
        Postal / Souvenir / Imán de nevera
        Pulsera de todo incluido / Buffet libre / Hotel de playa
        Tobogán acuático / Piscina de olas / Río lento
        Mochilero / Interrail / Viaje por carretera
        Nevera portátil / Bolsa de hielo / Cantimplora
        Pelota de playa / Palas / Frisbee
        Paseo marítimo / Muelle / Puerto deportivo
        Agua de coco / Zumo de piña / Granizada de limón
        Cometa / Molinillo / Pompas de jabón
        """
    ),
    WordPacks.pack(
        "games",
        "Juegos",
        "🎲",
        """
        Ajedrez / Damas / Backgammon
        Parchís / Oca / Serpientes y escaleras
        Monopoly / Catan / Risk
        Póker / Blackjack / Chinchón
        Mus / Tute / Brisca
        Uno / Dobble / Jungle Speed
        Trivial / Quiz / Adivinanzas
        Pictionary / Tabú / Mímica
        Escondite / Pilla pilla / Polis y cacos
        Comba / Goma elástica / Rayuela
        Canicas / Peonza / Yoyó
        Puzle / Sudoku / Crucigrama
        Sopa de letras / Ahorcado / Tres en raya
        Jenga / Dominó / Mikado
        Twister / Sillas musicales / Teléfono escacharrado
        Minecraft / Roblox / Fortnite
        Tetris / Candy Crush / Buscaminas
        FIFA / NBA 2K / Rocket League
        Mario Kart / Crash Bandicoot / Need for Speed
        Pokémon GO / Geocaching / Búsqueda del tesoro
        Among Us / Hombres lobo / Mafia
        Escape room / Gymkana / Cluedo
        Bingo / Lotería / Rasca y gana
        Cartas Pokémon / Cromos / Tazos
        Columpio / Tobogán / Balancín
        Piñata / Globos / Confeti
        Juego de rol / Dungeons & Dragons / Warhammer
        Scrabble / Apalabrados / Wordle
        Verdad o reto / Adivina quién / Simón dice
        Cubo de Rubik / Spinner / Slime
        """
    ),
    WordPacks.pack(
        "parties",
        "Fiestas",
        "🎉",
        """
        Navidad / Año Nuevo / Reyes
        Halloween / Día de Muertos / Carnaval
        Nochevieja / Uvas / Cotillón
        Árbol de Navidad / Belén / Calendario de adviento
        Roscón de Reyes / Panettone / Mazapán
        San Fermín / Fallas / Tomatina
        Feria de Abril / Romería / Semana Santa
        San Valentín / Día de la Madre / Día del Padre
        Boda / Bautizo / Comunión
        Despedida de soltero / Fiesta sorpresa / Baby shower
        Truco o trato / Pasaje del terror / Fiesta de disfraces
        Fuegos artificiales / Petardos / Bengalas
        Cabalgata / Procesión / Carroza
        Regalo / Sorpresa / Paquete
        Brindis / Discurso / Primer baile
        Muérdago / Acebo / Flor de Pascua
        Soplar las velas / Pedir un deseo / Cantar cumpleaños feliz
        Amigo invisible / Lotería de Navidad / Cesta de Navidad
        Noche de San Juan / Hoguera / Verbena de San Juan
        Carnaval de Venecia / San Patricio / Carnaval de Río
        Año Nuevo chino / Diwali / Holi
        Telaraña / Ataúd / Lápida
        Papel de regalo / Lazo / Tarjeta de felicitación
        Máscara / Antifaz / Maquillaje de zombi
        Pavo de Navidad / Cordero asado / Cochinillo
        Campanadas / Puerta del Sol / Cuenta atrás
        Carta a los Reyes / Carta a Papá Noel / Lista de deseos
        Gorro de Papá Noel / Cuernos de reno / Gorrito de cumpleaños
        Reno / Alce / Ciervo
        Huevos de Pascua / Mona de Pascua / Torrijas
        """
    )
)
