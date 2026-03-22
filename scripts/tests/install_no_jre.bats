#!/usr/bin/env bats
# Unit tests for scripts/install_no_jre.sh
#
# Run locally:  bats scripts/tests/install_no_jre.bats
# Run in CI:    see .github/workflows/ci.yml  (test-install-script job)

SCRIPT_DIR="$(cd "$(dirname "$BATS_TEST_FILENAME")/.." && pwd)"

setup() {
  # Source the script — main() is guarded by BASH_SOURCE and will NOT run
  # shellcheck source=../install_no_jre.sh
  source "${SCRIPT_DIR}/install_no_jre.sh"

  FAKE_HOME="$(mktemp -d)"
}

teardown() {
  rm -rf "${FAKE_HOME:-}"
}

create_os_release_file() {
  local contents="$1"
  local file
  file="$(mktemp)"
  printf '%s\n' "${contents}" > "${file}"
  echo "${file}"
}

# =============================================================================
# helper functions
# =============================================================================

@test "is_macos: returns success on Darwin" {
  _get_uname() { echo "Darwin"; }

  run is_macos
  [ "$status" -eq 0 ]
}

@test "is_macos: returns failure on Linux" {
  _get_uname() { echo "Linux"; }

  run is_macos
  [ "$status" -eq 1 ]
}

@test "detect_linux_os: returns debian when apt-get is available" {
  has_apt_get() { return 0; }
  has_dnf()     { return 1; }
  has_yum()     { return 1; }

  run detect_linux_os
  [ "$status" -eq 0 ]
  [ "$output" = "debian" ]
}

@test "detect_linux_os: returns amazon-linux when yum is available and os-release matches" {
  has_apt_get()     { return 1; }
  has_dnf()         { return 1; }
  has_yum()         { return 0; }
  is_amazon_linux() { return 0; }

  run detect_linux_os
  [ "$status" -eq 0 ]
  [ "$output" = "amazon-linux" ]
}

@test "detect_macos_os: returns macos-brew when brew is available" {
  has_brew() { return 0; }

  run detect_macos_os
  [ "$status" -eq 0 ]
  [ "$output" = "macos-brew" ]
}

@test "detect_macos_os: returns macos-no-brew when brew is not available" {
  has_brew() { return 1; }

  run detect_macos_os
  [ "$status" -eq 0 ]
  [ "$output" = "macos-no-brew" ]
}

@test "is_amazon_linux: returns success when os-release contains Amazon Linux 2" {
  local fake_os_release
  fake_os_release="$(create_os_release_file 'PRETTY_NAME="Amazon Linux 2"')"
  OS_RELEASE_FILE="${fake_os_release}"

  run is_amazon_linux
  [ "$status" -eq 0 ]

  rm -f "${fake_os_release}"
}

@test "is_amazon_linux: returns failure when os-release does not contain Amazon Linux 2" {
  local fake_os_release
  fake_os_release="$(create_os_release_file 'PRETTY_NAME="Ubuntu 24.04 LTS"')"
  OS_RELEASE_FILE="${fake_os_release}"

  run is_amazon_linux
  [ "$status" -eq 1 ]

  rm -f "${fake_os_release}"
}

# =============================================================================
# detect_os
# =============================================================================

@test "detect_os: returns 'debian' when apt-get is available" {
  _has_command() { [[ "$1" == "apt-get" ]]; }
  _get_uname()   { echo "Linux"; }

  run detect_os
  [ "$status" -eq 0 ]
  [ "$output" = "debian" ]
}

@test "detect_os: returns 'fedora' when dnf is available (no apt-get)" {
  _has_command() { [[ "$1" == "dnf" ]]; }
  _get_uname()   { echo "Linux"; }

  run detect_os
  [ "$status" -eq 0 ]
  [ "$output" = "fedora" ]
}

@test "detect_os: returns 'amazon-linux' when yum present and os-release has 'Amazon Linux 2'" {
  local fake_os_release
  fake_os_release="$(create_os_release_file 'PRETTY_NAME="Amazon Linux 2"')"

  _has_command() { [[ "$1" == "yum" ]]; }
  _get_uname()   { echo "Linux"; }
  OS_RELEASE_FILE="${fake_os_release}"

  run detect_os
  [ "$status" -eq 0 ]
  [ "$output" = "amazon-linux" ]

  rm -f "${fake_os_release}"
}

@test "detect_os: returns 'fedora-yum' when yum present but not Amazon Linux" {
  local fake_os_release
  fake_os_release="$(create_os_release_file 'PRETTY_NAME="CentOS Linux 7"')"

  _has_command() { [[ "$1" == "yum" ]]; }
  _get_uname()   { echo "Linux"; }
  OS_RELEASE_FILE="${fake_os_release}"

  run detect_os
  [ "$status" -eq 0 ]
  [ "$output" = "fedora-yum" ]

  rm -f "${fake_os_release}"
}

@test "detect_os: returns 'macos-brew' on Darwin when brew is available" {
  _has_command() { [[ "$1" == "brew" ]]; }
  _get_uname()   { echo "Darwin"; }

  run detect_os
  [ "$status" -eq 0 ]
  [ "$output" = "macos-brew" ]
}

@test "detect_os: returns 'macos-no-brew' on Darwin without brew" {
  _has_command() { return 1; }
  _get_uname()   { echo "Darwin"; }

  run detect_os
  [ "$status" -eq 0 ]
  [ "$output" = "macos-no-brew" ]
}

@test "detect_os: returns 'unknown' when no package manager and not Darwin" {
  _has_command() { return 1; }
  _get_uname()   { echo "Linux"; }

  run detect_os
  [ "$status" -eq 0 ]
  [ "$output" = "unknown" ]
}

# =============================================================================
# get_java_major_version
# =============================================================================

@test "get_java_major_version: returns 17 for OpenJDK 17" {
  _java_version_output() { echo 'openjdk version "17.0.9" 2023-10-17'; }

  run get_java_major_version
  [ "$status" -eq 0 ]
  [ "$output" = "17" ]
}

@test "get_java_major_version: returns 21 for OpenJDK 21" {
  _java_version_output() { echo 'openjdk version "21.0.1" 2023-10-17'; }

  run get_java_major_version
  [ "$status" -eq 0 ]
  [ "$output" = "21" ]
}

@test "get_java_major_version: returns 0 when java is not available" {
  _java_version_output() { return 1; }

  run get_java_major_version
  [ "$status" -eq 0 ]
  [ "$output" = "0" ]
}

# =============================================================================
# ensure_java
# =============================================================================

@test "install_java_for_os: dispatches to install_java_fedora_yum" {
  install_java_fedora_yum() { echo "fedora_yum_install_called"; }

  run install_java_for_os "fedora-yum"
  [ "$status" -eq 0 ]
  [[ "$output" == *"fedora_yum_install_called"* ]]
}

@test "install_java_for_os: exits 1 on unknown OS" {
  run install_java_for_os "unknown"
  [ "$status" -eq 1 ]
  [[ "$output" == *"ERROR"* ]]
}

@test "ensure_java: exits 0 and skips install when Java 17 is present" {
  get_java_major_version() { echo "17"; }
  install_java_debian()    { echo "SHOULD_NOT_BE_CALLED"; }

  run ensure_java
  [ "$status" -eq 0 ]
  [[ "$output" != *"SHOULD_NOT_BE_CALLED"* ]]
}

@test "ensure_java: exits 0 and skips install when Java 21 is present" {
  get_java_major_version() { echo "21"; }
  install_java_debian()    { echo "SHOULD_NOT_BE_CALLED"; }

  run ensure_java
  [ "$status" -eq 0 ]
  [[ "$output" != *"SHOULD_NOT_BE_CALLED"* ]]
}

@test "ensure_java: calls install_java_debian on debian when java absent" {
  get_java_major_version() { echo "0"; }
  detect_os()              { echo "debian"; }
  install_java_debian()    { echo "debian_install_called"; }

  run ensure_java
  [ "$status" -eq 0 ]
  [[ "$output" == *"debian_install_called"* ]]
}

@test "ensure_java: calls install_java_fedora on fedora when java absent" {
  get_java_major_version() { echo "0"; }
  detect_os()              { echo "fedora"; }
  install_java_fedora()    { echo "fedora_install_called"; }

  run ensure_java
  [ "$status" -eq 0 ]
  [[ "$output" == *"fedora_install_called"* ]]
}

@test "ensure_java: calls install_java_amazon on amazon-linux when java absent" {
  get_java_major_version() { echo "0"; }
  detect_os()              { echo "amazon-linux"; }
  install_java_amazon()    { echo "amazon_install_called"; }

  run ensure_java
  [ "$status" -eq 0 ]
  [[ "$output" == *"amazon_install_called"* ]]
}

@test "ensure_java: calls install_java_brew on macos-brew when java absent" {
  get_java_major_version() { echo "0"; }
  detect_os()              { echo "macos-brew"; }
  install_java_brew()      { echo "brew_install_called"; }

  run ensure_java
  [ "$status" -eq 0 ]
  [[ "$output" == *"brew_install_called"* ]]
}

@test "ensure_java: exits 1 on macos-no-brew when java absent (prints error)" {
  get_java_major_version() { echo "0"; }
  detect_os()              { echo "macos-no-brew"; }

  run ensure_java
  [ "$status" -eq 1 ]
  [[ "$output" == *"ERROR"* ]]
}

@test "ensure_java: exits 1 on unknown OS when java absent" {
  get_java_major_version() { echo "0"; }
  detect_os()              { echo "unknown"; }

  run ensure_java
  [ "$status" -eq 1 ]
  [[ "$output" == *"ERROR"* ]]
}

# =============================================================================
# ensure_unzip
# =============================================================================

@test "install_unzip_for_os: dispatches to install_unzip_yum for fedora-yum" {
  install_unzip_yum() { echo "yum_unzip_called"; }

  run install_unzip_for_os "fedora-yum"
  [ "$status" -eq 0 ]
  [[ "$output" == *"yum_unzip_called"* ]]
}

@test "install_unzip_for_os: exits 1 on unknown OS" {
  run install_unzip_for_os "unknown"
  [ "$status" -eq 1 ]
  [[ "$output" == *"ERROR"* ]]
}

@test "ensure_unzip: exits 0 and skips install when unzip is present" {
  _has_command()        { [[ "$1" == "unzip" ]]; }
  install_unzip_debian() { echo "SHOULD_NOT_BE_CALLED"; }

  run ensure_unzip
  [ "$status" -eq 0 ]
  [[ "$output" != *"SHOULD_NOT_BE_CALLED"* ]]
}

@test "ensure_unzip: calls install_unzip_debian on debian when unzip absent" {
  _has_command()        { return 1; }
  detect_os()           { echo "debian"; }
  install_unzip_debian() { echo "debian_unzip_called"; }

  run ensure_unzip
  [ "$status" -eq 0 ]
  [[ "$output" == *"debian_unzip_called"* ]]
}

@test "ensure_unzip: calls install_unzip_fedora on fedora when unzip absent" {
  _has_command()        { return 1; }
  detect_os()           { echo "fedora"; }
  install_unzip_fedora() { echo "fedora_unzip_called"; }

  run ensure_unzip
  [ "$status" -eq 0 ]
  [[ "$output" == *"fedora_unzip_called"* ]]
}

@test "ensure_unzip: calls install_unzip_yum on amazon-linux when unzip absent" {
  _has_command()    { return 1; }
  detect_os()       { echo "amazon-linux"; }
  install_unzip_yum() { echo "yum_unzip_called"; }

  run ensure_unzip
  [ "$status" -eq 0 ]
  [[ "$output" == *"yum_unzip_called"* ]]
}

@test "ensure_unzip: calls install_unzip_brew on macos-brew when unzip absent" {
  _has_command()     { return 1; }
  detect_os()        { echo "macos-brew"; }
  install_unzip_brew() { echo "brew_unzip_called"; }

  run ensure_unzip
  [ "$status" -eq 0 ]
  [[ "$output" == *"brew_unzip_called"* ]]
}

@test "ensure_unzip: exits 1 on unknown OS when unzip absent" {
  _has_command() { return 1; }
  detect_os()    { echo "unknown"; }

  run ensure_unzip
  [ "$status" -eq 1 ]
  [[ "$output" == *"ERROR"* ]]
}

# =============================================================================
# setup_path
# =============================================================================

@test "path_contains_bin_dir: returns success when BIN_DIR is already on PATH" {
  BIN_DIR="/test/steply/bin"
  local saved_path="$PATH"
  PATH="/usr/bin:/test/steply/bin:/bin"

  run path_contains_bin_dir
  [ "$status" -eq 0 ]

  PATH="$saved_path"
}

@test "path_contains_bin_dir: returns failure when BIN_DIR is missing from PATH" {
  BIN_DIR="/test/steply/bin"
  local saved_path="$PATH"
  PATH="/usr/bin:/bin"

  run path_contains_bin_dir
  [ "$status" -eq 1 ]

  PATH="$saved_path"
}

@test "shell_profiles: returns profile, bashrc and zshrc under HOME" {
  HOME="${FAKE_HOME}"

  run shell_profiles
  [ "$status" -eq 0 ]
  [ "$output" = "${FAKE_HOME}/.profile
${FAKE_HOME}/.bashrc
${FAKE_HOME}/.zshrc" ]
}

@test "append_path_to_profile: appends export line when profile exists and path is not already listed" {
  local profile="${FAKE_HOME}/.bashrc"
  touch "${profile}"
  BIN_DIR="/test/steply/bin"

  run append_path_to_profile "${profile}" 'export PATH="/test/steply/bin:$PATH"'
  [ "$status" -eq 0 ]
  grep -q '# Added by Steply installer' "${profile}"
  grep -q '/test/steply/bin' "${profile}"
}

@test "append_path_to_profile: does not modify profile when path is already listed" {
  local profile="${FAKE_HOME}/.bashrc"
  printf 'export PATH="/test/steply/bin:$PATH"\n' > "${profile}"
  BIN_DIR="/test/steply/bin"
  local initial_content
  initial_content="$(cat "${profile}")"

  run append_path_to_profile "${profile}" 'export PATH="/test/steply/bin:$PATH"'
  [ "$status" -eq 0 ]
  [ "$(cat "${profile}")" = "${initial_content}" ]
}

@test "setup_path: appends BIN_DIR to .bashrc when not already in PATH" {
  touch "${FAKE_HOME}/.bashrc"
  HOME="${FAKE_HOME}"
  BIN_DIR="/test/steply/bin"
  local saved_path="$PATH"
  PATH="/usr/bin:/bin"

  setup_path

  PATH="$saved_path"
  grep -q "/test/steply/bin" "${FAKE_HOME}/.bashrc"
}

@test "setup_path: appends BIN_DIR to .zshrc when not already in PATH" {
  touch "${FAKE_HOME}/.zshrc"
  HOME="${FAKE_HOME}"
  BIN_DIR="/test/steply/bin"
  local saved_path="$PATH"
  PATH="/usr/bin:/bin"

  setup_path

  PATH="$saved_path"
  grep -q "/test/steply/bin" "${FAKE_HOME}/.zshrc"
}

@test "setup_path: does not modify .bashrc when BIN_DIR already in PATH" {
  touch "${FAKE_HOME}/.bashrc"
  local initial_content
  initial_content="$(cat "${FAKE_HOME}/.bashrc")"

  HOME="${FAKE_HOME}"
  BIN_DIR="/test/steply/bin"
  local saved_path="$PATH"
  PATH="/usr/bin:/test/steply/bin:/bin"

  setup_path

  PATH="$saved_path"
  [ "$(cat "${FAKE_HOME}/.bashrc")" = "${initial_content}" ]
}

@test "setup_path: does not append to .bashrc when BIN_DIR already listed in file" {
  echo 'export PATH="/test/steply/bin:$PATH"' > "${FAKE_HOME}/.bashrc"
  local initial_content
  initial_content="$(cat "${FAKE_HOME}/.bashrc")"

  HOME="${FAKE_HOME}"
  BIN_DIR="/test/steply/bin"
  local saved_path="$PATH"
  PATH="/usr/bin:/bin"

  setup_path

  PATH="$saved_path"
  [ "$(cat "${FAKE_HOME}/.bashrc")" = "${initial_content}" ]
}
