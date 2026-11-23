# Sourced by the signing scripts. project_dir <name> prints the folder that holds a project's
# keystore.properties: apps/<name> for an app, engine/<name> for an engine game; nothing if neither.
project_dir() {
  local root
  root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
  if [ -d "$root/apps/$1" ]; then
    echo "$root/apps/$1"
  elif [ -f "$root/engine/$1/project.godot" ] && [ "$1" != core ]; then
    echo "$root/engine/$1"
  fi
}
