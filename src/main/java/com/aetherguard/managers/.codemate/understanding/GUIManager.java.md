The `GUIManager` class in the `com.aetherguard.managers` package is responsible for managing all graphical user interfaces (GUIs) related to the AetherGuard anti-cheat plugin. It handles the creation, opening, and closing of GUIs for players.

Key functionalities include:
- Opening a specified GUI for a player by name, with optional additional data.
- Creating GUI instances based on the GUI name (though the current implementation returns null).
- Closing all open GUIs for all online players on the server.

The class interacts with the main `AetherGuard` plugin instance to access configuration messages and server player information. It ensures players receive feedback if a requested GUI is not found.