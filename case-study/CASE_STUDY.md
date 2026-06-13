# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

[ Answer ]
The main challenge here is that one warehouse serves multiple stores and one store can be served by multiple warehouses. So when a cost comes in — rent, labor, transport — it's not immediately clear who it belongs to.
Before building anything, I'd want to know how costs are captured today and whether we can already trace a shipment back to a specific warehouse and store. Without that foundation, any allocation is just guesswork.
My starting point would be to attach a cost center to each Warehouse and Store, and record a cost entry every time a fulfillment event happens. Start with a simple rule like splitting costs by units handled. We can make it smarter once the data is clean and trusted.


## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

[ Answer ]
You can't cut what you can't see. First step is always understanding where the money is going — warehouse space, labor, and transport are usually the top three.
The quickest wins come from underutilization. Our `Warehouse` already has `capacity` and `stock` fields. A simple report comparing those two numbers shows which warehouses are paying for space they're not using. That's often the easiest cost to reduce.
After that, I'd look at routing — if a store is being served by a distant warehouse when a closer one has availability, that's unnecessary transport cost. Then longer term, renegotiate contracts based on actual usage data rather than estimates.


## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

[ Answer ]
The problem with no integration is that someone has to manually move data between systems. That's slow and mistakes happen.
The goal is simple: when something happens in the fulfillment system, the financial system should know about it automatically. A warehouse created, stock moved, a warehouse archived — each of these should produce a cost event that flows directly into finance without manual work.
This is the same idea we applied in Task 2 — notify the downstream system only after the change is confirmed. Same pattern here, just with a financial system on the receiving end.
I'd also make sure events are idempotent (no duplicate entries if the same event is sent twice) and run a daily reconciliation to catch anything that slipped through.


## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
[ Answer ]
Good forecasting needs good history. The `Warehouse` entity already has `createdAt` and `archivedAt`, so we know how long each warehouse has been active. Attach cost records to that and you have a baseline to work from.
From there, forecasting is straightforward: average monthly cost per warehouse, apply a growth factor based on expected volume, add known one-off costs like planned replacements.
The more useful thing is linking operational decisions to budget impact. If someone wants to add a warehouse at a location that's already near its max capacity, the system should surface that cost implication immediately — not at the end of the quarter.


## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
[ Answer ]
When a warehouse is replaced, the old one is archived with an `archivedAt` timestamp and the new one takes over with the same Business Unit Code. That shared BU code is what keeps the cost history connected across both.
We don't delete the old record — we archive it. So when finance asks what MWH.001 cost over the past 3 years, they can see costs from both the old and the new warehouse through the same BU code. Nothing is lost.
The replacement itself also has a cost — moving stock, transition time, any overlap period. That should be recorded as a specific entry at the point of replacement so it's visible and not buried in general costs.
The system already enforces that the new warehouse must hold the same stock as the old one. That keeps the inventory numbers clean across the transition, which matters a lot when finance is reconciling stock values.


## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
