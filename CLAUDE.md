# Java Coding Standards and Guidelines

## Architecture Principles

- Follow Clean Architecture principles to separate concerns
- Structure code in layers (presentation, domain, data) with clear boundaries
- Maintain unidirectional dependencies (dependencies point inward)
- Use interfaces to define boundaries between architectural layers
- Apply SOLID principles in all code development:
    - **S**ingle Responsibility Principle: Each class has one reason to change
    - **O**pen/Closed Principle: Open for extension, closed for modification
    - **L**iskov Substitution Principle: Subtypes must be substitutable for base types
    - **I**nterface Segregation Principle: Clients shouldn't depend on methods they don't use
    - **D**ependency Inversion Principle: Depend on abstractions, not concretions

## Project Structure

- Organize packages by feature rather than by layer
- Keep consistent module structure across the codebase
- Group related functionality in logical modules
- Use appropriate granularity for microservices (if applicable)
- Follow the standard Maven/Gradle project structure

## Build and Dependency Management

### Maven Commands

- Always use the Maven Wrapper (`mvnw`) instead of direct `mvn` command
- Always include the Maven settings file when running Maven commands:
  ```bash
  ./mvnw -s .mvn/settings.xml [command]
  ```
- Execute tests from the root directory using the -pl argument to specify the module:
  ```bash
  ./mvnw -s .mvn/settings.xml -pl <module-name> test -Dtest=<TestClass>#<testMethod>
  ```

### Dependency Management

- Declare managed dependencies in parent POM
- Use dependency versions compatible with Java version in use
- Avoid transitive dependencies when possible
- Regularly check for dependency vulnerabilities and updates
- Use BOM (Bill of Materials) for consistent dependency versions

## Code Style and Organization

### Naming Conventions

- Use descriptive class and method names that clearly convey purpose
- Follow camelCase for variables, method names, and package names
- Use PascalCase for class names and interfaces
- Use ALL_CAPS for constants and static final fields
- Use noun or noun phrases for class names (e.g., `OrderProcessor`, `UserRepository`)
- Use verb or verb phrases for method names (e.g., `processOrder()`, `findUser()`)
- Prefix interface implementations with "Impl" or more descriptive terms (e.g.,
  `DefaultUserService`)
- Use meaningful, specific names (avoid generic terms like `Manager`, `Processor` without context)

### Formatting

- Maintain consistent indentation (4 spaces, not tabs)
- Limit line length to 120 characters
- Use appropriate whitespace between operators, statements, and blocks
- Organize imports alphabetically and remove unused imports
- Place opening braces on the same line for classes, methods, and control structures
- **REQUIRED**: Insert empty lines to separate logical code blocks within methods and classes
- **MANDATORY**: Always separate logical operations within a try block with an empty line, and
  always have empty lines after try/catch/finally opening braces and before their closing braces (as
  shown in the examples)
- **CRITICAL**: Empty lines are not optional - they are a required part of the code structure and
  must be included exactly as specified in example patterns
- Format try-catch-finally blocks consistently:
  ```java
  try {
    // First operation
    // <keep new line here>
    // Related operations together
    // Multiple related lines
    // <keep new line here>
    // Next logical operation group
    // <keep new line here>
  } catch (Exception e) {
    // <keep new line here>
    log.error("Failed to process operation, error: {}", e.getMessage(), e);
    // <keep new line here>
    throw e;
    // <keep new line here>
  } finally { // add this finally block only if needed by the business logic
    // <keep new line here>
    log.info("Operation completed, status: {}", status);
  }
  ```

### Code Organization

- Arrange class members in a logical order:
    1. Static fields
    2. Instance fields
    3. Constructors
    4. Public methods
    5. Protected/package-private methods
    6. Private methods
- Group related functionality together
- Keep methods focused and cohesive (under 30 lines when possible)
- Limit class size (consider refactoring large classes)
- Minimize method parameters (use parameter objects for multiple parameters)

## Implementation Patterns

### General Patterns

- Prefer composition over inheritance
- Program to interfaces, not implementations
- Use dependency injection rather than direct instantiation
- Use Java records for immutable data holder classes:
  ```java
  // Prefer this (Java record)
  private record Point(int x, int y) {}
  
  // Instead of this (regular class)
  private static class Point {
    private final int x;
    private final int y;
    
    Point(int x, int y) {
      this.x = x;
      this.y = y;
    }
    
    // Plus getters, equals, hashCode, toString
  }
  ```
- Apply the Builder pattern for complex object creation
- Use Factory pattern for object creation with complex logic
- Implement the Strategy pattern for varying algorithms
- Apply Template Method pattern for algorithmic skeletons

### REST API DTOs

- Always use these annotations for all REST API DTOs:
  ```java
  import jakarta.validation.constraints.NotNull;
  import lombok.Builder;

  import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

  @Builder(toBuilder = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record XyzDto(...) {}
  ```

### Design Principles

- Follow the DRY principle (Don't Repeat Yourself)
- Design for testability (separate side effects from pure logic)
- Encapsulate what varies
- Favor immutability where appropriate
- Keep methods and classes focused on a single responsibility
- Use final for parameters and variables that shouldn't change

### Performance Considerations

- Use appropriate data structures for operations
- Consider time and space complexity in algorithms
- Avoid premature optimization
- Profile before optimizing
- Consider caching for expensive operations
- Use lazy initialization when appropriate
- Be mindful of resource utilization (connections, threads, memory)

## Error Handling

- Use checked exceptions only when the caller can reasonably recover
- Use unchecked exceptions for programming errors and unrecoverable situations
- Create custom exception hierarchies for domain-specific errors
- Always include meaningful context in exception messages
- Avoid catching generic Exception unless absolutely necessary
- Don't suppress exceptions without proper handling
- Don't use exceptions for normal control flow
- Include the original exception when wrapping exceptions:
  ```java
  try {
    // Operation that might fail
    // <keep new line here>
  } catch (SomeException e) {
    // <keep new line here>
    throw new CustomException("Failed to process operation", e);
  }
  ```

## Logging

- Use SLF4J for all logging
- Configure appropriate log levels for different environments
- Follow consistent logging patterns:
    - Log messages should have clear descriptive text followed by labeled parameters
    - Example: `"Starting process, paramName: {}, otherParam: {}"`
- Format error logs consistently:
  `log.error("Failed to {}, error: {}", operation, e.getMessage(), e);`
- Apply the same format for warning logs
- Always include the exception object in log methods
- Use the appropriate log level:
    - ERROR: Exception or error that impacts functionality
    - WARN: Unexpected situations that don't cause failure
    - INFO: Significant application events
    - DEBUG: Detailed information useful for debugging
    - TRACE: Very detailed information

## Testing

### General Testing Principles

- Write tests before or alongside production code (TDD when possible)
- Test both positive and negative scenarios
- Keep tests independent and idempotent
- Maintain high test coverage for business logic
- Use integration tests for component interactions
- Include end-to-end tests for critical paths

### Unit Testing

- All services should have corresponding test classes
- Use mocks or stubs for external dependencies
- Test boundary conditions and edge cases
- Use descriptive test method names (e.g., `shouldThrowExceptionWhenUserIsNull`)
- Structure tests using AAA pattern (Arrange-Act-Assert) or Given-When-Then
- Use AssertJ for assertions with the fluent API style:
  ```java
  // Prefer this (AssertJ)
  assertThat(someValue).isEqualTo(expectedValue);
  
  // Instead of this (JUnit)
  assertEquals(expectedValue, someValue);
  ```

- Import AssertJ assertions using:
  ```java
  import static org.assertj.core.api.Assertions.assertThat;
  ```

- Use JsonAssertions for JSON content:
  ```java
  import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
  ```

### Test Structure

- Organize tests to mirror production code structure
- Use appropriate test frameworks (JUnit 5, TestNG)
- Apply test fixtures and parameterized tests for multiple scenarios
- Use test helpers and builders to simplify test setup
- Structure tests by behavior rather than by method

## Documentation

- Document public APIs with JavaDoc
- Include purpose, parameters, return values, and exceptions in JavaDoc
- Keep documentation up-to-date with code changes
- Document complex algorithms and business rules
- Write self-documenting code using descriptive names
- Avoid code comments for readable code, but comment complex or non-obvious logic
- Include README files for modules explaining purpose and usage

## Git Workflow and Code Review

### Git Practices

- Branch naming: `jira-ticket-brief-description`
- Keep commit messages concise and single-line
- Format commit messages as: `JIRA-1234 Short descriptive message`
- Avoid multiline commit messages
- Make small, focused commits
- Rebase feature branches on main before creating pull requests
- Delete branches after merging

### Code Review Guidelines

- Ensure code follows project architecture and structure
- Verify proper error handling and edge cases
- Check for adequate test coverage
- Review for performance and scalability concerns
- Look for code smells and technical debt
- Validate security practices
- Ensure code meets team standards
- Provide constructive feedback with clear rationale

## Security Best Practices

- Validate all input data
- Use parameterized queries for database operations
- Implement proper authentication and authorization
- Sanitize output to prevent XSS attacks
- Apply secure password storage using proper hashing algorithms
- Protect sensitive data with encryption
- Use secure communication protocols
- Apply the principle of least privilege
- Follow OWASP guidelines for secure coding

## Concurrency and Multithreading

- Use higher-level concurrency utilities instead of raw threads
- Consider thread safety in shared data structures
- Use atomic operations for simple shared state
- Apply appropriate synchronization mechanisms
- Be aware of deadlock and race conditions
- Use thread pools for managing multiple tasks
- Consider non-blocking algorithms where appropriate
- Document thread safety assumptions