#!/usr/bin/env bash

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_4c444768151a_key -iv $encrypted_4c444768151a_iv -in codesigning.asc.enc -out codesigning.asc -d
    gpg --fast-import ci/codesigning.asc
fi
