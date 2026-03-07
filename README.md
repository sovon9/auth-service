# auth-service
user authentication service


### SecurityConfig.java
First SecurityFilterChain (Authorization Server)

This filter chain is responsible for securing OAuth2 Authorization Server endpoints only.

It turns your application into an OAuth2 Authorization Server.<br>

<b>What This Filter Chain Handles</b>

It handles only OAuth2-related endpoints such as:
```
/oauth2/authorize
/oauth2/token
/oauth2/jwks
/oauth2/introspect
/oauth2/revoke
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




