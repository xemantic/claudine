# Claudine Memory Tool

The Memory tool allows Claudine to store and retrieve information across sessions. This feature enables Claudine to remember important facts, user preferences, and previous interactions, providing a more personalized experience.

## Features

The Memory tool provides the following operations:

- **Store**: Save information with a specific key
- **Retrieve**: Access previously stored information using a key
- **List**: View all available memory keys
- **Delete**: Remove a specific memory entry

## Implementation Details

Memories are stored in a JSON file (`.claudine_memory.json`) in the user's home directory. The file contains a simple key-value map structure that persists across Claudine sessions.

## Usage Examples

### Storing Information

Claudine can remember information you want to access later:

```
[me]> Please remember that my favorite color is blue
[Claudine]> I'll store that information for future reference.
```

Behind the scenes, Claudine uses the Memory tool with the 'store' operation, saving this preference with a key like 'user_favorite_color'.

### Retrieving Information

In a future session, Claudine can recall this information:

```
[me]> What's my favorite color?
[Claudine]> Based on what you've told me before, your favorite color is blue.
```

Claudine uses the Memory tool with the 'retrieve' operation to access this stored information.

### Listing Stored Memories

You can ask Claudine to show what information is being stored:

```
[me]> What information do you remember about me?
[Claudine]> I have the following information stored:
- user_favorite_color
- user_birthday
- preferred_programming_language
```

### Deleting Memories

You can also ask Claudine to forget specific information:

```
[me]> Please forget my favorite color
[Claudine]> I've removed that information from my memory.
```

## Privacy Considerations

All memory data is stored locally on your machine in the `.claudine_memory.json` file. No information is sent to external servers beyond what's needed for the normal operation of Claude AI.