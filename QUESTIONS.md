# Questions

Here we have 3 questions related to the code base for you to answer. 
It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. 
   If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```
Yes, I would refactor toward a single, consistent strategy.

The codebase currently has three distinct approaches.
If I were maintaining this codebase, I would refactor Store and Product toward the Warehouse approach.

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` 
API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. 
What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches work, but they come with different trade-offs.                                                                                                         
                                                                                                                                                                         
  With the OpenAPI-first approach (example Warehouse API), the spec is the source of truth. 
  The implementation is generated from it, so the documentation can never drift from the actual behaviour. 
  It also makes it easier to work in parallel — frontend or other teams can look at the YAML and know exactly what to expect before the 
  backend is even done. The downside is that it adds a code generation step to the build, which can be a bit awkward with IDEs and CI, and you lose some control over how
  the generated code looks.                                                                                                                                              
                                                                                                                                                                         
  With code-first (example Store and Product), you move faster at the start. You just write the endpoint and you're done. 
  But the documentation becomes a separate concern that tends to get ignored, and the API can change without any clear signal to consumers.                                                                               
                                                                                                                                                                         
  My preference is OpenAPI-first, especially for any API that other teams or systems depend on. The YAML file is a contract, and having that contract explicit and       
  version-controlled is worth the small overhead. I would apply the same approach to Store and Product — define the spec first, generate the interface, and implement    
  behind it. 
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? 
Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I think about tests in terms of confidence vs. cost. Not every test gives you the same value.                                                                          
                                                                                                                                                                         
  The first thing I would focus on is the use case layer — CreateWarehouseUseCase, ReplaceWarehouseUseCase, and so on. 
  This is where all the real business logic lives. Because these can be tested with plain JUnit and mocked ports, no database   
  needed. They're fast, isolated, and they test the things that actually matter if they break.                                                                           
  REST layer, I would keep tests focused on the golden path and the most important error cases.  
  
  To keep coverage useful over time, whenever a bug reaches production, I write a regression test before fixing it. 
  That way coverage grows toward where failures actually happen, not just toward hitting a percentage.             
```