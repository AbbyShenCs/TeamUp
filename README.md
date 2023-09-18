<<<<<<< HEAD
# TeamUp: A Team Matching System
## Project Description
A mobile website designed to help users find like-minded partners. The main features include user registration & login, profile updates, user search based on tags, and team formation.

## Technology Stack
### Frontend
Vue 3
Vant UI Library (for mobile)
Vite Scaffold
TypeScript
Axios for HTTP requests

### Backend
Java SpringBoot 2.7.x
MySQL Database
MyBatis-Plus
MyBatis X Auto-generation
Redis Cache (Implemented with Spring Data Redis and other methods)
Redisson for Distributed Locking
Swagger + Knife4j for API Documentation
Gson for JSON serialization

## Project Highlights
- User Authentication: Leveraged Redis for distributed Session management, addressing login status synchronization across clusters. Adopted Hash storage over String for user data, leading to a 5% memory saving and facilitating individual field updates.
- Collection Processing: Employed Java 8 Stream API and Lambda expressions for handling complex collection operations, such as associating users with their respective teams.
- Data Import Efficiency: Used Easy Excel for reading collected user data and boosted database import performance through a custom thread pool combined with CompletableFuture for concurrent programming. Benchmarks showed a reduction in data import time for 1 million rows from 20 seconds to 7 seconds. (Actual results may vary)
- Caching Strategy: Cached frequently accessed user info lists on the homepage with Redis, reducing API response time from 800ms to 120ms. Developed a custom Redis serializer to tackle data distortion and space wastage issues.
- Cache Pre-warming: Addressed slow homepage load times for first-time visitors by pre-warming caches with Spring Scheduler's timed tasks. Ensured tasks aren't redundantly executed in multi-node deployments using distributed locks.
- Operational Idempotence: Used Redisson's distributed lock to prevent users from joining a team multiple times or exceeding the team limit, ensuring idempotent operations.
- Automatic API Documentation: Integrated Knife4j and Swagger for auto-generating backend API documentation. Annotations like ApiOperation further enriched the documentation, obviating manual upkeep.
- Frontend Consistency: Adopted the Vant UI library and crafted a universal Layout component, standardizing the layout across the main, search, and team pages while cutting down redundant code.
- Dynamic Navigation Titles: Utilized Vue Router's global route guard to switch navigation bar titles dynamically based on the page. This was achieved by extending the title field in the global route configuration, reducing unnecessary conditional code.
=======
# TeamUp
A Team Matching System
>>>>>>> 6931a18324eea72feb4f1065e3ce4cffce36c924
