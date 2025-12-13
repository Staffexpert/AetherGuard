# CrossPlayerAnalyzer

A utility class designed to detect potential multi-accounting by analyzing and comparing player behaviors in a Minecraft server environment.

## Key Features

- **Behavior Recording:** Stores behavior data for players using their unique IDs.
- **Multi-Accounting Detection:** Compares the current player's behavior against previously recorded behaviors to estimate the likelihood of multi-accounting.
- **Similarity Thresholds:** Uses predefined similarity thresholds to assign suspicion scores (0.0, 60.0, or 90.0) indicating the confidence level of multi-accounting.
- **Thread-Safe Storage:** Utilizes a concurrent hash map to safely handle behavior data in multi-threaded contexts.

## Components

- **behaviors:** A thread-safe map holding `PlayerBehavior` instances keyed by player UUID strings.
- **recordPlayerBehavior(Player):** Captures and stores the behavior of a given player.
- **detectMultiAccounting(Player):** Calculates similarity between the current player's behavior and all stored behaviors, returning a suspicion score based on similarity thresholds.
- **calculateSimilarity(PlayerBehavior, PlayerBehavior):** Placeholder method intended to compute similarity between two player behaviors (currently returns a fixed value).
- **PlayerBehavior (inner class):** Encapsulates player-specific data such as IP address and client brand for behavior comparison.

## Usage Context

This class is intended for use in Minecraft server plugins to help identify players who may be using multiple accounts by analyzing behavioral patterns such as IP addresses and client information.