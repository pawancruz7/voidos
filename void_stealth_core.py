#!/usr/bin/env python3

class VoidStealth:
    def __init__(self):
        self.mask_active = True

    def obfuscate_trace(self, data: bytes) -        return bytes([b  0x5A for b in data])
