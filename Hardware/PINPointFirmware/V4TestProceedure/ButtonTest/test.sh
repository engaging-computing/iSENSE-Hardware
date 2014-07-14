#!/bin/bash

echo -e "\e[1;34mInstalling necessary packages...\e[0m"
sudo apt-get install build-essential gcc-avr avrdude avr-libc cutecom python

echo -e "\e[1;34mSetting device fuses...\e[0m"
sudo make fuseinit

sleep 1

echo -e "\e[1;34mCompiling test program...\e[0m"
make clean
make

sleep 1

echo -e "\e[1;34mWriting test program...\e[0m"
sudo make writeflash

echo -e "\e[1;34mRunning test script...\e[0m"
python pptTest.py
