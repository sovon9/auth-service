# auth-service
user authentication service


### SecurityConfig.java
First SecurityFilterChain (Authorization Server)

This filter chain is responsible for securing OAuth2 Authorization Server endpoints only.

It turns your application into an OAuth2 Authorization Server.<br>

<b>What This Filter Chain Handles</b>

It handles only OAuth2-related endpoints such as:
```
  ## Your Authorization Server endpoints (base: http://localhost:9000)

   Endpoint                                                                              │ Purpose
  ───────────────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────────────────
   POST /oauth2/token                                                                    │ Get an access token
   GET /oauth2/authorize                                                                 │ Start authorization code flow
   GET /oauth2/jwks                                                                      │ Public keys (for resource servers to verify JWTs)
   GET /.well-known/oauth-authorization-server                                           │ Discovery metadata
```
It does NOT handle:
```
/api/**
/test
/actuator/**
```
Any normal controller endpoints <br>
Those are handled by the second filter chain.

#### @Order(1)
This chain is checked FIRST. <br>
If request matches it → Spring stops and uses this chain.<br>
Order(2) will NOT run.

#### securityMatcher(...)
```.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())```

This limits this filter chain to:<br>
Only OAuth2 Authorization Server endpoints.<br>

Without this:<br>
This chain would match ALL requests.<br>
Your second filter chain would never execute.<br>

#### .with(authorizationServerConfigurer, ...)

This installs the entire OAuth2 Authorization Server machinery:<br>

* Registers `/oauth2/authorize<br>`
* Registers `/oauth2/token<br>`
* Adds token `generation logic<br>`
* Adds validation filters
* Adds consent handling
* Adds protocol validation

Without this: <br>
Your app is NOT an Authorization Server.<br>
Those endpoints do not exist.<br>

#### .authorizeHttpRequests(...)
```.anyRequest().authenticated() ```

Inside this filter chain,
“anyRequest” means:<br>
Any request that matched the OAuth2 endpoints.<br>
It does NOT mean all application requests.<br>

This ensures:<br>
`/oauth2/authorize` requires authentication <br>
OAuth endpoints are protected

## Flow 1 — demo-client (Client Credentials)

Use case: Backend service calling another service — no user involved.

POST http://localhost:9000/oauth2/token

In Postman:

• Method: POST
• Tab → Body → x-www-form-urlencoded

Key                                                     │ Value
─────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────
grant_type                                              │ client_credentials
client_id                                               │ demo-client
client_secret                                           │ demo-secret
scope                                                   │ demo.read

│ Since demo-client uses CLIENT_SECRET_POST, credentials go in the body, not Basic auth header.

Response you'll get:

    {
      "access_token": "eyJhbGci...",
      "token_type": "Bearer",
      "expires_in": 599,
      "scope": "demo.read"
    }
    ──────
## Flow 2 — demo-app (Authorization Code)

Use case: A real user logging in via browser.

### Step 1 — Open this URL in your browser:

    http://localhost:9000/oauth2/authorize?response_type=code&client_id=demo-app&redirect_uri=http://127.0.0.
1:9000/login/authorized&scope=demo.read

→ You'll be redirected to the login page
→ Login with your seeded admin user: admin / changeme
→ After login, the browser redirects to:

    http://127.0.0.1:9000/login/authorized?code=SOME_AUTH_CODE

Copy that code value.

### Step 2 — Exchange the code for a token

POST http://localhost:9000/oauth2/token

In Postman:

• Tab → Authorization → Type: Basic Auth
• Username: demo-app
• Password: demo-app-secret
• Tab → Body → x-www-form-urlencoded

Key                                                     │ Value
────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────
grant_type                                              │ authorization_code
code                                                    │ (paste the code from Step 1)
redirect_uri                                            │ http://127.0.0.1:9000/login/authorized

Response:

    {
      "access_token": "eyJhbGci...",
      "refresh_token": "eyJhbGci...",
      "token_type": "Bearer",
      "expires_in": 599,
      "scope": "demo.read"
    }


