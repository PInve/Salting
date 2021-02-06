# Salting
An example on how to store your users' passwords and retrieve them with hashing and salting.

## Hashing

Hashing is the process of generating a string, or hash, from a given message using a mathematical function known as a cryptographic hash function.

While there are several hash functions out there, those tailored to hashing passwords need to have four main properties to be secure:

    1. It should be deterministic: the same message processed by the same hash function should always produce the same hash
    2. It's not reversible: it's impractical to generate a message from its hash
    3. It has high entropy: a small change to a message should produce a vastly different hash
    4. And it resists collisions: two different messages should not produce the same hash

A hash function that has all four properties is a strong candidate for password hashing since together they dramatically increase the difficulty in reverse-engineering the password from the hash.

Also, though, password hashing functions should be slow. A fast algorithm would aid brute force attacks in which a hacker will attempt to guess a password by hashing and comparing billions (or trillions) of potential passwords per second.

Some great hash functions that meet all these criteria are **PBKDF2, BCrypt, and SCrypt**.

We'll use the first in this example.


## What is salting

In cryptography, salting refers to adding random data to the input of a hash function to guarantee a unique output, the hash, even when the inputs are the same.

Consequently, the unique hash produced by adding the salt can protect us against different attack vectors, such as rainbow table attacks, while slowing down dictionary and brute-force attacks.

Let’s say that we have password ***farm1990M0O*** and the salt ***f1nd1ngn3m0***.

We can salt that password by either appending or prepending the salt to it.

For example: ***farm1990M0Of1nd1ngn3m0*** or ***f1nd1ngn3m0farm1990M0O*** are valid salted passwords.

Once the salt is added, we can then hash it.

Let's say two of our users want to use the same password: both salted passwords would hash to the same value.
But, if we choose another salt for the same password, we get two unique and longer passwords that hash to a different value.

> Different users, same password. Different salts, different hashes.

We'll store in our database each user's username and generated hash along with its salt, ***NOT*** the users' passwords themselves.

When the user logs in, we can lookup the username, append the salt to the provided password, hash it, and then verify if the stored hash matches the computed hash.

## Project setup

This example relies upon Spring Boot and some of its libraries to expose REST endpoints and interact with a MySQL database.

Project Lombok is used but not strictly necessary - although it takes care of Java's boilerplate code.

### Dependencies

- Spring-boot-started-data-jpa
- Spring-boot-starter-web
- mysql-conncector-java
- lombok

### Configuration

To run this project, we'll have to set a few keys in the ***application.properties*** file.

- *spring.datasource.url*: URL to your database schema (
- *spring.datasource.password*: password to use for database access
- *spring.datasource.username*: username to use for database access
- *spring.jpa.hibernate.ddl-auto*: defaults to update; whether Hybernate should reflect, or not, the changes in your @Entity classes on the matching database's tables (if possible).
- *server:port*: defaults to 8080; the port on which we'll be bale to contact our endpoints.

### Database

Spring data JPA will ***NOT*** create the database schema for us.

On the other hand, it will create our users table in it and keep it in sync.

We'll just use one table for users, containing the following fields:
- BIGINT id
- VARCHAR username
- VARCHAR email
- VARCHAR token

### Endpoints

Through the ***UserController*** class, we'll expose two endpoints at the following URLs:

1. *localhost:{server.port}/users/new*
2. *localhost:{server.port}/users/login*

You can use the likes of [Postman](https://www.postman.com/) to send requests.

### Create a new user

The first endpoint will accept POST requests carrying a JSON body such as:

>{
    "username": "MRossi",
    "email": "mrossi@gmail.com",
    "password": "Mrossi1234$%",
    "passwordCheck": "Mrossi1234$%"
}

Validation will be applied on the provided values and, if everything runs smooth, an user will be created and the token for its password stored in the database.
We can check the JSON response to verify this or the presence of errors.

### Login a user

The second enpoint will accept POST requests as well, carrying this JSON keys:

>{
    "username": "MRossi",
    "password": "Mrossi1234$%"
}

We'll use the username to retrieve the user's token in our DB, which will be used to validate the password.

We can check the JSON response for the outcome of our login attempt.


## Token structure

The ***PasswordAuthentication*** class is responsible for the creation of tokens and validation of passwords against them.
Inside this class, we've defined some constants:

- PREFIX_ID: a String that will be appended to the token and lets us know which class generated it.
- DEFAULT_COST: cost represents the exponential computational cost of hashing a password, 0 to 30. Default is 16 (2^16 iterations).
- ALGORITHM: the algorithm to use for hashing. PBKDF2WithHmacSHA512 in our case.
- SIZE: expected hash size in bytes.
- SALT_SIZE: size of our salt in bytes.
- layout: defines the regexp used to retrieve data from our token

The token will have this structure once it is stored in our database:

> PREFIX_ID + COST + $ + HASH

We know the first SALT_SIZE bytes of our hash to be the randomly generated salt added to the password.

### Token creation

When we send a POST request to create a new user, we provide a String representing the desired password.

The following steps will follow to turn it into our token:
1. Generate SALT_SIZE random bytes
2. Hash both the password and the salt with the PBKDF2WithHmacSHA512 algorithm, using cost defined iterations, to obtain an array of bytes.
3. Store the original salt bytes followed by the hash bytes in an array.
4. Encode this array in a String (Base64 encoding used in the example).
5. Prepend the PREFIX_ID and cost as in the token structure shown above.
6. Save the token in our database.


### Authentication

After providing the username and password to the login endpoint, the following steps take place to authorize our user:
1. Find the user in our DB with the provided username.
2. Retrieve the user's token.
3. Check the token has our layout - in other words: that it is correctly formed.
4. Use a regexp capture group to extract from the token the cost defined during the hashing process.
5. Extract the hash portion of the token with another regexp capture group.
6. Decode the hash from a String to a bytes array (Base64 decoding used in this example).
7. We know the first SALT_SIZE bytes to be our added salt.
8. Hash the provided password and the extracted salt togheter with the PBKDF2WithHmacSHA512 algorithm, using cost defined iterations, to obtain an array of bytes.
9. Check the bytes obtained in this way against the token's hash portion bytes following the salt bytes.
9. If they are the same, the password is correct. If any byte is different, the password is incorrect. 

The following lines in
> public boolean authenticate(char[] password, String token)

ensure that even an *incorrect* password will take the same time to compute of a correct one.
This is to prevent [timing attacks](https://en.wikipedia.org/wiki/Timing_attack).

```
int zero = 0;
for (int idx = 0; idx < check.length; ++idx)
	zero |= hash[salt.length + idx] ^ check[idx];
return zero == 0;




