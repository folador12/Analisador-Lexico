global main
extern printf
extern scanf

section .text
main: 	; Entrada do programa
	push ebp
	mov ebp, esp
	sub esp, 20
	sub esp, 12
	push dword[ebp - 4]
	push 10
	pop eax
	cmp dword [ESP], eax
	jle rotuloFalsoREL1
	mov dword [ESP], 1
	jmp rotuloSaidaREL2
rotuloFalsoREL1: 	mov dword [ESP], 0
rotuloSaidaREL2: 	cmp dword[esp], 0
	je rotuloElse3
	add esp, 4
	push 100
	pop eax
	mov dword[ebp - 8], eax
	jmp rotuloFimIf4
rotuloElse3:
	add esp, 4
	push dword[ebp - 8]
	push 2
	pop eax
	imul eax, dword [ESP]
	mov dword [ESP], eax
	pop eax
	mov dword[ebp - 8], eax
rotuloFimIf4: 	push dword[ebp - 8]
	push @Integer
	call printf
	add esp, 8
	push 10
	pop eax
	mov dword[ebp - 12], eax
	push dword[ebp - 12]
	push @Integer
	call printf
	add esp, 8
	push 2
	push 5
	push 3
	pop eax
	imul eax, dword [ESP]
	mov dword [ESP], eax
	pop eax
	add dword[ESP], eax
	pop eax
	mov dword[ebp - 12], eax
	push rotuloString5
	call printf
	add esp, 4
	push dword[ebp - 12]
	push @Integer
	call printf
	add esp, 8
	lea eax, [ebp - 12]
	push eax
	push @Integer
	call scanf
	add esp, 8
	lea eax, [ebp - 16]
	push eax
	push @Integer
	call scanf
	add esp, 8
	push dword[ebp - 12]
	push dword[ebp - 16]
	pop eax
	imul eax, dword [ESP]
	mov dword [ESP], eax
	pop eax
	mov dword[ebp - 20], eax
	push rotuloString6
	call printf
	add esp, 4
	push dword[ebp - 20]
	push @Integer
	call printf
	add esp, 8
	lea eax, [ebp - 12]
	push eax
	push @Integer
	call scanf
	add esp, 8
	lea eax, [ebp - 16]
	push eax
	push @Integer
	call scanf
	add esp, 8
	push dword[ebp - 12]
	push 10
	pop eax
	cmp dword [ESP], eax
	jl rotuloFalsoREL7
	mov dword [ESP], 1
	jmp rotuloSaidaREL8
rotuloFalsoREL7: 	mov dword [ESP], 0
rotuloSaidaREL8: 	cmp dword[esp], 0
	je rotuloElse9
	add esp, 4
	push dword[ebp - 12]
	push @Integer
	call printf
	add esp, 8
	jmp rotuloFimIf10
rotuloElse9:
	add esp, 4
	push dword[ebp - 16]
	push @Integer
	call printf
	add esp, 8
rotuloFimIf10: 	push 1
	pop eax
	mov dword[ebp - 32], eax
rotuloWhile11: 	push dword[ebp - 32]
	push 10
	pop eax
	cmp dword [ESP], eax
	jg rotuloFalsoREL13
	mov dword [ESP], 1
	jmp rotuloSaidaREL14
rotuloFalsoREL13: 	mov dword [ESP], 0
rotuloSaidaREL14: 	cmp dword[esp], 0
	je rotuloFimWhile12
	add esp, 4
	push dword[ebp - 32]
	push @Integer
	call printf
	add esp, 8
	push dword[ebp - 32]
	push 1
	pop eax
	add dword[ESP], eax
	pop eax
	mov dword[ebp - 32], eax
	jmp rotuloWhile11
rotuloFimWhile12: 	add esp, 4
	push 1
	pop eax
	mov dword[ebp - 32], eax
rotuloRepeat15: 	push dword[ebp - 32]
	push @Integer
	call printf
	add esp, 8
	push dword[ebp - 32]
	push 1
	pop eax
	add dword[ESP], eax
	pop eax
	mov dword[ebp - 32], eax
	push dword[ebp - 32]
	push 10
	pop eax
	cmp dword [ESP], eax
	jle rotuloFalsoREL16
	mov dword [ESP], 1
	jmp rotuloSaidaREL17
rotuloFalsoREL16: 	mov dword [ESP], 0
rotuloSaidaREL17: 	cmp dword[esp], 0
	je rotuloRepeat15
	add esp, 4
	push 1
	pop dword[ebp - 32]
	push 10
rotuloFOR18: 	push ecx
	mov ecx, dword[ebp - 32]
	cmp ecx, dword[esp+4]
	jg rotuloFIMFOR19
	pop ecx
	push dword[ebp - 32]
	push @Integer
	call printf
	add esp, 8
	add dword[ebp - 32], 1
	jmp rotuloFOR18
rotuloFIMFOR19: 	add esp, 8
	push rotuloString20
	call printf
	add esp, 4
	lea eax, [ebp - 24]
	push eax
	push @Integer
	call scanf
	add esp, 8
	push rotuloString21
	call printf
	add esp, 4
	lea eax, [ebp - 28]
	push eax
	push @Integer
	call scanf
	add esp, 8
	push dword[ebp - 24]
	push 0
	pop eax
	cmp dword [ESP], eax
	jle rotuloFalsoREL22
	mov dword [ESP], 1
	jmp rotuloSaidaREL23
rotuloFalsoREL22: 	mov dword [ESP], 0
rotuloSaidaREL23: 	push dword[ebp - 28]
	push 0
	pop eax
	cmp dword [ESP], eax
	jle rotuloFalsoREL24
	mov dword [ESP], 1
	jmp rotuloSaidaREL25
rotuloFalsoREL24: 	mov dword [ESP], 0
rotuloSaidaREL25: 	cmp dword [ESP + 4], 1
	jne rotuloFalsoMTL27
	pop eax
	cmp dword [ESP], eax
	jne rotuloFalsoMTL27
	mov dword [ESP], 1
	jmp rotuloSaidaMTL26
rotuloFalsoMTL27: 	mov dword [ESP], 0
rotuloSaidaMTL26: 	cmp dword[esp], 0
	je rotuloElse28
	add esp, 4
	push rotuloString30
	call printf
	add esp, 4
	push rotuloStringLN
	call printf
	add esp, 4
	jmp rotuloFimIf29
rotuloElse28:
	add esp, 4
	push rotuloString31
	call printf
	add esp, 4
	push rotuloStringLN
	call printf
	add esp, 4
rotuloFimIf29: 	leave
	ret

section .data

@Integer: db '%d',0
rotuloString5: db 'Valor: ',0
rotuloString6: db 'Resultado: ',0
rotuloString20: db 'Informe a: ',0
rotuloString21: db 'Informe b: ',0
rotuloString30: db 'Positivos',0
rotuloStringLN: db '',10,0
rotuloString31: db 'Um dos valores não é positivo',0
