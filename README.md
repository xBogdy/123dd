# Multi ML

This repository contains multi modloader project templates for different versions of minecraft. Simply clone the project and use the desired version branch as starting point for your project.

> Do not forget to update to your own *remote* with `git remote set-url origin <remote_url>`.

## How to get started

Once you have checked out to your desired version branch (e.g. `git checkout 1.19.2`) the first thing you should do is to define some of the properties describing your project. These are mostly located in the `gradle.properties` file but some are also reflected by the project structure itself. Following adjustments are important:

| Property          | Description                                                          | Points of interest                                                                                                                                                                                                                                                                                                                                     | Default value          | Requires change |
| ----------------- | -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------- | --------------- |
| `mod_name`        | Name of the mod                                                      | field in `gradle.properties`; `MOD_NAME` field in `common/src/main/java/com/gitlab/srcmc/mymodid/ModCommon.java`; mentioned in license header of source files                                                                                                                                                                                          | `Example Mod`          | yes             |
| `mod_id`          | Id of the mod                                                        | field in `gradle.properties`; `MOD_ID` field in `common/src/main/java/com/gitlab/srcmc/mymodid/ModCommon.java`; name of `common/src/main/resources/mixin.*.json` file; name of the primary packages for `common/src/main/java/com/gitlab/srcmc/*`, `fabric/src/main/java/com/gitlab/srcmc/fabric/*` and `forge/src/main/java/com/gitlab/srcmc/forge/*` | `mymodid`              | yes             |
| `mod_author`      | Author of the mod                                                    | field in `gradle.properties`                                                                                                                                                                                                                                                                                                                           | `JoeDoe`               | yes             |
| `mod_version`     | Current version of the mod; You may handle this value to your likings | field in `gradle.properties`                                                                                                                                                                                                                                                                                                                           | `0.1.0-alpha`          | no              |
| `mod_description` | A short description of the mod                                       | field in `gradle.properties`                                                                                                                                                                                                                                                                                                                           | `A mod for Minecraft.` | no              |
| `group`           | Maven group                                                          | field in `gradle.properties`; Reflects package structure of `common/src/main/java/*/mymodid`, `fabric/src/main/java/*/fabric/mymodid` and `forge/src/main/java/*/forge/mymodid`                                                                                                                                                                        | `com.gitlab.srcmc`     | yes             |
| `license`         | The license of this project                                          | field in `gradle.properties`; Short description located in header of source files                                                                                                                                                                                                                                                                      | `GNU-LGPL-3`           | no              |

You may at any point merge your changes into different version branches or discard other branches altogether if you dont need them.

## How to test locally

The project is setup to provide distinct run configurations for up to 2 clients and 1 server per mod loader. Following gradle commands can be used to run the project in a dev environment:

| Command                     | Description                                         |
| --------------------------- | --------------------------------------------------- |
| `gradlew fabric:runClient`  | Runs the first client with the `fabric` mod loader  |
| `gradlew fabric:runClient2` | Runs the second client with the `fabric` mod loader |
| `gradlew fabric:runServer`  | Runs the server with the `fabric` mod loader        |
| `gradlew forge:runClient`   | Runs the first client with the `forge` mod loader   |
| `gradlew forge:runClient2`  | Runs the second client with the `forge` mod loader  |
| `gradlew forge:runServer`   | Runs the server with the `forge` mod loader         |

> Append the `--console=plain` flag if the console output is gibberish.

## How to publish

The first step would be to build your project, which can be archieved with `gradlew build`. The resulting jar files for the different mod loaders are located at `common/build/libs`, `fabric/build/libs` and `forge/build/libs`. You may distribute those files to your liking.

A `publish` script is provided which can be used to build and publish the project to [Curseforge](https://www.curseforge.com/). A Curseforge api key and the project id must be defined with the environment variables `CURSE_API_KEY` and `CURSE_PROJECT_ID`.

**Usage:**

```bash
publish [<mod_version>] [<minecraft_version>]
```

If either version is not provided it is inferred from the projects configuration files. Otherwise said configuration files are updated with the given version. The release type is inferred from the suffix of the mod version (i.e. a suffix of '-alpha' or '-beta' will result in an alpha or beta release, otherwise a full release is published).

> The script publishes by default only for the `forge` mod loader. This can be adjusted by modifying the `mod_loaders` array at the top of the script (in the *config* section).
