# Git Remote Change Alert for Eclipse

<p align="center"><b>git_remote_change_alert</b> is an Eclipse plugin designed to alert users when there are changes in the remote Git repositories of their projects. This plugin is helpful for developers who want to keep their local repositories updated with the latest changes from remote repositories.</p>

## Features

- **Startup Check**: Scans all the open projects in the workspace during startup and checks for remote Git changes.
- **Project Open Check**: When a project is opened in Eclipse, it checks if the project has any remote Git changes.
- **Notification**: Displays a notification to users about which projects have remote changes that might need pulling.

## Configuration

You can configure the cache and cooldown period for fetching remote changes:

1. Go to '**Window** > **Preferences**'.
2. Navigate to '**Git Remote Change Alert**'.
3. Adjust the cache and cooldown periods as needed. Setting them to **0** disables them.

## Troubleshooting

The logs will be written to the Eclipse log file, which you can typically find in the .metadata/.log file in your workspace directory. You can also view logs from within Eclipse in the Error Log view.

## Installation

1. Clone this repository
2. Open '**Eclipse**' and go to '**File**' > '**Import**'.
3. Select '**General**' > '**Existing Projects into Workspace**'.
4. Navigate to the cloned repository and import the project.
5. Once imported, right-click on the project and choose '**Export**'.
6. Select '**Plug-in Development**' > '**Deployable plug-ins and fragments**'.
7. Choose an export destination and click '**Finish**'.
8. Copy the exported JAR file to the '**dropins**' directory of your '**Eclipse**' installation.
9. Restart '**Eclipse**'.

## Usage

Once installed, the plugin will automatically check for remote Git changes during Eclipse startup and whenever a project is opened.

If there are any remote changes, a notification will be displayed with details about which projects have changes in the remote repositories.
