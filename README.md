# EverGreen Readme

![](https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/evergreen_logo__and_name.png?alt=media&token=e44efadc-09d5-475c-9bce-9a2a3f7732e6)

**Table of Contents**
- [Description](#Description)
- [Objective](#Objective)
- [Wireframes](#Wireframes)
- [Database](#Database)



# Description

Due to climate change, the scarcity of natural resources and the increasing pollution worldwide, now we are getting more aware about how important recycling is to counteract the environmental impact.

However, the great disadvantage of traditional recycling is that it focuses on a linear model of consumption, in other words, a product is acquired to later fulfill its main usage and to be subsequently recycled, ending its liveclycle.

Evergreen, my project, was born from looking for an alternative to replace the linear consumption model, promoting a more efficient alternative called upcycling. Upcycling is putting to use products, materials or waste to manufacture new materials or products of higher quality, greater ecological value and greater economic value.

Evergreen is based on a free application to be implemented for mobile devices with Android operating system. Evergreen aims to empower people to actively promote ideas related with upcycling through posts on the platform.

Evergreen also will provide a digital communication method for artists and artisans to offer efficient alternatives to reuse and transform materials that could otherwise be wasted for remuneration. In addition, an advertising system will be implemented within the application for its monetization.


<div align="center"> 
  <img align="center" src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/many_hands_holding_garbage.jpg?alt=media&token=bfd073be-4cbf-45d8-a3f9-4fe5add58632" width="500">
</div>

# Objective

Develop an application for mobile devices with Android operating system through the Android Studio IDE implementing publications with access to a real-time chat so that users can share upcycling ideas and can also save their favorite publications. Users will be able to create a user profile and customize it.

Create special publications for artists and craftsmen where they can propose upcycling solutions in exchange for remuneration. Any user can become an upcycling artist and craftsman at Evergreen.

Finally, it intends to implement a system of sponsored publications for the monetization of the app.

All this will be done with Kotlin as the programming language and Google Firebase services to implement a real-time database, user authentication through email, cloud storage of multimedia files and notification management between devices.

<div aling="center">
	<img src="https://kotlinlang.org/assets/images/open-graph/kotlin_250x250.png" width="130">
	<img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRYZqbWr38Wfa0y-H9rYZLc-l0uvlVvThTJzQ&usqp=CAU" width="90">
</div>

# Wireframes

- The user must choose between logging in or registering on the platform. The user can register with email, Facebook account or Google account. The user will also have the option of recovering the password if it has been forgotten.


<div align="center"> 
  <img src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/Wireframes%2FEverGreen%20Login%20WireFrame.png?alt=media&token=8a78c078-40a4-4a1e-8639-35d1b388e2a9" width="500">
  <br>
</div>

- The user will have a user profile section where they can personalize their profile, create new posts to share their upcycling ideas, and create proposals as an artist or craftsman for remuneration.


<div align="center"> 
  <img src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/Wireframes%2FPrivateProfile.png?alt=media&token=daf9fc58-4d85-40e7-90d0-cb39190ef89e" width="200">
  <br>
</div>

- The user can see if the other person is online and if a message sent has been read. Chat massages will support images and text. The user will be able to see the history of chats created.


<div align="center"> 
  <img src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/Wireframes%2FInbox.png?alt=media&token=5f8c4dab-0a87-4aa7-82e5-7307e1862112" width="200">
  <img src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/Wireframes%2FInboxDetails.png?alt=media&token=ecf9b7ff-982f-49e7-9898-c84422ef2a39" width="200">
  <br>
</div>


- The home section will display all posts relevant to the user including upcycling ideas posts, paid proposals, and sponsored posts. The user will be able to search by categories and see the details of each publication.


<div align="center"> 
  <img src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/Wireframes%2FHome.png?alt=media&token=95ccc4ba-bf61-4a50-ab34-42b73a80bcee" width="200">
  <img src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/Wireframes%2FPublicEcopropuestaDetails.png?alt=media&token=62a55eb8-ce3a-40d7-b9ba-0497a16783f3" width="200">
  <br>
</div>


- The user will be able to store their favorite publications in this section.


<div align="center"> 
  <img src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/Wireframes%2FFavorites.png?alt=media&token=c793992a-0abd-4c7a-b13a-ec1683dec8d0" width="200">
  <br>
</div>

- **Application workflow**



<div align="center"> 
  <img src="https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/Wireframes%2FEverGreen%20MainActivity%20WireFrame.png?alt=media&token=74824ff7-d91f-48d6-b222-49a5b32db8ac">
  <br>
</div>

# Database

- The data is Stored and synced with a NoSQL cloud database. Data is synced across all clients in realtime, and remains available when your app goes offline.

- The Firebase Realtime Database is a cloud-hosted database. Data is stored as JSON and synchronized in realtime to every connected client.

## Database Structure

```
{
  // Main users data
  "users": {
    "user1": {
      "uid": "idairaID",
      "profile": {
        "username": "Idairas",
        "email": "idairas@fakemail.com",
        "profileImage": "url//...",
        "coverImage": "url//..."
      },
      "online": false,
      "search": "idaira",
      "createdAt": 1459361875666,
      // An index to track Idaira's created posts
      "createdPosts": {
        // the value here doesn't matter, just that the key exists
         "post2": true,
         "post3": true
      },
      // An index to track Idaira's favorite posts
      "favoritePosts": {
         "post1": true,
         "post5": true
      }
    },
    "user2": { ... },
    "user3": { ... }
  },

  // Main posts data
  "posts": {
    "post1": {
      "coverImage": "url//...1",
      // Type could be:
      //// UPCYCLING_SERVICE = 1,
      //// UPCYCLING_IDEA = 2,
      //// SPONSORED_AD = 3
      "type": 1,
      "minPrice": 12,
      "title": "Esculturas con materiales reciclados",
      "postId": "postid",
      "publisher": "user2",
      "upcyclingCategories": {
        "11": true,
        "2": true
      },
      "createdAt": 1459361875666,
      "updatedAt": 1459361875666,
      "description": "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      "images": {
         "url//...1": true,
         "url//...2": true,
         "url//...e3": true,
      },
      // This is a necessary redundancy for two-way relationships on JSON.
      "membersFollowingAsFavorite": {
        "user1": true,
        "user3": true,
      },
      "sponsorProfile": {
        "brandImage": "url//...",
        "title": "coche 100% electrico!",
        "linkText": "descubrenos"
      },

    },
    "post2": { ... },
    "post3": { ... }
  },

  // Upcycling Categories to filter posts
  "upcyclingCategories" : {
    "1": {
      "name" : "Cartón",
      "description" : "Reciclar cartón es la opción fácil, pero ¿alguna vez ha pensado en hacer Upcycling con cajas de cartón? Es fuerte, versátil y casi siempre disponible, así que aquí tienes algunas ideas de Upcycling de cartón para que pruebes."
    },
    "2": {
      "name" : "Vidrio",
      "description" : "Todos conocemos el sonido de un montón de vidrios rompiéndose al ser depositados en el camión de reciclaje. ¿Por qué no buscar nuevos usos para botellas de vidrio, frascos e incluso ventanas antes de ponerlos inmediatamente en el contenedor de reciclaje?"
    },
    "3": {
      "name" : "Plástico",
      "description" : "Una botella de plástico vacía es el símbolo por excelencia de nuestra sociedad derrochadora, upcycling es una forma de darle una nueva vida útil al omnipresente plástico."
    },
    "4": {
      "name" : "Joyas",
      "description" : "Las joyas viejas y rotas son una de las cosas más divertidas para hacer Upcycling. Aquí hay algunas ideas para hacer Upcycling de joyas rotas que no se pueden reparar."
    },
    "5": {
      "name" : "Tela de desecho",
      "description" : "Hay varias formas de terminar con tela de desecho. La principal es la costura o cuando se desgasta la tela de nuestras sábanas, toallas y manteles. Prepárate porque hay muchas formas de utilizar pequeños trozos de tela y textiles de desecho, así como trozos más grandes de tela no deseada."
    },
    "6": {
      "name" : "Jeans",
      "description" : "Los jeans viejos obviamente caen en la categoría de ropa, pero hay tantas opciones para reciclar sus viejos jeans, chaquetas, faldas o pantalones cortos que se merecían una categoría propia."
    },
    "7": {
      "name" : "Poliestireno",
      "description" : "¿No se te hunde el corazón cuando abres algo que has comprado y te das cuenta de que está envuelto en espuma de poliestireno o algún otro tipo de embalaje de espuma o gránulos?"
    },
    "8": {
      "name" : "Ropa",
      "description" : "Todos compramos demasiada ropa. La moda es voluble y la ropa se desgasta, se rompe o se mancha. Donar ropa que no uses es una buena opción, pero cuando está en mal estado quizás es hora de aplicar upcycling."
    },
    "9": {
      "name" : "Madera",
      "description" : "Es posible que se sorprenda de lo fácil que es terminar con madera no deseada. Hay muchas cosas que puede hacer para hacer Upcycling de madera, con las herramientas adecuadas."
    },
    "10": {
      "name" : "Papel",
      "description" : "Afortunadamente, muchos de nosotros ahora recibimos extractos bancarios electrónicos, pero de alguna manera eso no parece reducir la cantidad de correo basura, volantes y circulares de tiendas que se depositan en el buzón todos los días."
    },
    "11": {
      "name" : "Metal",
      "description" : "Todos sabemos sobre la abundancia de latas en los contenedores de reciclaje, pero hay tantas cosas que puedes hacer con ellas que la próxima vez que las deseches lo pensaras dos veces."
    },
    "12": {
      "name" : "Muebles",
      "description" : "Afortunadamente, el reciclaje de muebles se está volviendo cada vez más popular e incluso se ha convertido en negocios exitosos para muchos emprendedores individuales, como los/las Artistas y Artesanos que ofrecen sus servicios en nuestra plataforma."
    },
    "13": {
      "name" : "Cachivaches",
      "description" : "Básicamente son solo cosas rotas o fuera de estilo que heredamos o compramos (¡y luego nos arrepentimos!) Que no encajan en ninguna de las otras categorías."
    },
    "14": {
      "name" : "Otros",
      "description" : "Cualquier material al que se le pueda aplicar Upcycling que no hayamos incluido en nuestra lista de categorías."
    }

  },

  // Chats contains only meta info about each conversation
  // stored under the chats's unique ID
  "chats": {
    "one": {
      "postID":"post1"
      "postTitle": "Vasos de Botellas Cortadas",
      "postImageURL": "url//...",
      "lastMessage": "mlolo: yo estoy bien y tu?.",
      "isSeen": false
      "timestamp": 1459361875666
    },
    "two": { ... },
    "three": { ... }
  },

  // Conversation members are easily accessible
  // and stored by chat conversation ID
  "chatMembers": {
    // we'll talk about indices like this below
    "one": {
      "user1": true,
      "user2": true
    },
    "two": { ... },
    "three": { ... }
  },

  // Messages are separate from data we may want to iterate quickly
  // but still easily paginated and queried, and organized by chat
  // conversation ID
  "chatMessages": {
    "one": {
      "m1": {
        "sender": "idairaID",
        "message": "hola como estas?.",
        "iseen": true,
        "url": "url//...",
        "timestamp": 1459361875337
      },
      "m2": { ... },
      "m3": { ... }
    },
    "two": { ... },
    "three": { ... }
  }
}

```



