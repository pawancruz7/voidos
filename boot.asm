[org 0x7c00]    ; BIOS bootloader standard memory offset

    ; Screen ko clear karne aur setup karne ke liye
    mov ah, 0x0e    ; BIOS Teletype output mode
    mov bx, msg     ; Message ka address 'bx' register me load karein

print_loop:
    mov al, [bx]    ; Message ka pehla character 'al' me dalein
    cmp al, 0       ; Check karein agar string khatam ho gayi hai (0/Null)
    je done         ; Agar 0 hai, toh 'done' par jump karein
    int 0x10        ; BIOS Video Interrupt call karein jo screen par print karega
    inc bx          ; Agle character par jayein
    jmp print_loop  ; Loop ko repeat karein

done:
    cli             ; Interrupts ko clear/disable karein
    hlt             ; CPU ko halt (stop) kar dein

msg:
    db 'Welcome to VoidOS...', 0   ; Aapka OS welcome message

times 510 - ($ - $$) db 0   ; File size ko exactly 510 bytes banane ke liye zeros add karein
dw 0xaa55                   ; Boot signature (BIOS ko batata hai ki ye bootable file hai)
