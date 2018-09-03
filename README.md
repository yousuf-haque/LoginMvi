# LoginMvi
An Example of an MVI application

For the main component, check out [the login component](https://github.com/yousuf-haque/LoginMvi/tree/master/app/src/main/java/com/yohaq/loginmvi/presentation/login)


`LoginController` is the equivalent of `LoginFragment`
I prefer Conductor (and its controllers) to fragments as they are very similar to fragments but have a nicer API. Mainly the transactions are synchronous, controllers persist configuration changes, and conductor has rich backastack manipulation.

The main portions of the MVI cycle are:

1. `LoginController`
    - The View rendering piece that hooks into the rest of the android view hierarchy
    - Handles observing `Observable<ViewState>` and updates itself accordingly
2. `LoginModel`
    - Assembles the stream of `LoginState`
    - Handles interaction from the user, or from sources of data from the rest of the app (repos, network, disk, etc)
3. `LoginView`
    - Takes state objects and renders them to view state objects
    - Translates domain data to visual data
4. `LoginIntent`
    - Thin value type classes that carry commands from the UI to the `model`
5. `LoginDI`
    - An Implementation detail of the dependency injection
    - Wires up the view state stream using repositories, and pices of app data and pipes them into the model function to build the state stream
