{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";
    custom_nixpkgs.url = "github:toasterofbread/nixpkgs/4df73973bda897522847e03e0820067c053bccad";
    android-nixpkgs.url = "github:HPRIOR/android-nixpkgs/516bd59caa6883d1a5dad0538af03a1f521e7764";
  };

  outputs = { self, nixpkgs, custom_nixpkgs, android-nixpkgs, ... }:
    let
      system = "x86_64-linux";
    in
    {
      devShells."${system}".default =
        let
          pkgs = import nixpkgs {
            inherit system;
          };
          custom_pkgs = import custom_nixpkgs {
            inherit system;
          };

          android-sdk = (android-nixpkgs.sdk.${system} (sdkPkgs: with sdkPkgs; [
            cmdline-tools-latest
            build-tools-34-0-0
            platform-tools
            platforms-android-34
          ]));
        in
        pkgs.mkShell {
          packages = with pkgs; [
            jdk17
            android-sdk
            (custom_pkgs.kotlin-native-toolchain-env.override { x86_64 = true; aarch64 = true; })
          ];

          JAVA_HOME = "${pkgs.jdk17}/lib/openjdk";

          shellHook = ''
            export KONAN_DATA_DIR=$(pwd)/.konan
            mkdir -p $KONAN_DATA_DIR
            cp -asfT ${custom_pkgs.kotlin-native-toolchain-env} $KONAN_DATA_DIR
            chmod -R u+w $KONAN_DATA_DIR
          '';
        };
    };
}
