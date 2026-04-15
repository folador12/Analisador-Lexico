global main
extern printf
extern scanf

section .text
main: 	; Entrada do programa
	push ebp
	mov ebp, esp
	sub esp, 4
	push 5
	pop eax
	mov dword[ebp - 4], eax
	push dword[ebp - 4]
	push 10
	pop eax
	cmp dword [ESP], eax
	jge rotuloFalsoREL1
	mov dword [ESP], 1
	jmp rotuloSaidaREL2
rotuloFalsoREL1: 	mov dword [ESP], 0
rotuloSaidaREL2: 	cmp dword[esp], 0
	je rotuloElse3
	add esp, 4
	push rotuloString5
	call printf
	add esp, 4
	push rotuloStringLN
	call printf
	add esp, 4
	jmp rotuloFimIf4
rotuloElse3:
	add esp, 4
	push rotuloString6
	call printf
	add esp, 4
	push rotuloStringLN
	call printf
	add esp, 4
rotuloFimIf4: 	leave
	ret

section .data

rotuloString5: db 'Valor menor que 10',0
rotuloStringLN: db '',10,0
rotuloString6: db 'Valor maior ou igual a 10',0
