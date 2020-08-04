# Currency-Converter
Reactive Currency Converter written using Clean Architecture Pattern, MVVM, Koin, Coroutines, Livedata with Unit Testing and Integration Testing done 


- Fully written in Kotlin
- Implemented Search UI with Bottom Sheet
- SearchView is focused as soon as opened for better UX
- Empty view is shown when search result provides no result
- Proper error handling done for retrofit calls
- It is not safe to store access key in code, it will be easy to decompile and take it out. But still doing it here due to scope of work. Better security approach would be to use a proxy server and make API calls through them. We can store the access key in a trusted and secure location
- DB is the single source of truth, network calls even after fetched are saved to the db and then fetched
- Cache is abstracted to swap in case different implementation is thought of. Retrofit cache is not used since it is very preliminary
- Repository layer is also abstracted to avoid hard coupling
- Search with currency code,currency name or currency value
- Livedata used only in Viewmodel and Activity since Android dependency shouldn't leak into domain hence Flow is used
- Using Livedata which enables proper handling of rotation and other configuration change
- Can be refreshed by pulling down the grid view
- Previously searched value is maintained to provide good ux
- Only USD to Other currency conversion is available in API, so rest are done through calculation
- Everything is reactive
- Used Koin for DI
- Both Unit Testing and Integration Testing are written
