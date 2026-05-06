global main
extern printf
extern scanf

section .text
main: 	; Entrada do programa
	push ebp
	mov ebp, esp
	sub esp, 4
	push 1
	pop dword[ebp - 4]
	push 10
rotuloFOR1: 	push ecx
	mov ecx, dword[ebp - 4]
	cmp ecx, dword[esp+4]
	jg rotuloFIMFOR2
	pop ecx
	push dword[ebp - 4]
	push @Integer
	call printf
	add esp, 8
	push rotuloStringLN
	call printf
	add esp, 4
	add dword[ebp - 4], 1
	jmp rotuloFOR1
rotuloFIMFOR2: 	add esp, 8
	leave
	ret

section .data

@Integer: db '%d',0
rotuloStringLN: db '',10,0
