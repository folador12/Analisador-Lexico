global main
extern printf
extern scanf

section .text
main: 	; Entrada do programa
	push ebp
	mov ebp, esp
	sub esp, 20
	sub esp, 12
	jmp rotuloInicioCorpo1
rotuloInicioCorpo1: 	push dword[ebp - 4]
	push 10
	pop eax
	cmp dword [ESP], eax
	jle rotuloFalsoREL2
	mov dword [ESP], 1
	jmp rotuloSaidaREL3
rotuloFalsoREL2: 	mov dword [ESP], 0
rotuloSaidaREL3: 	cmp dword[esp], 0
	je rotuloElse4
	add esp, 4
	push 100
	pop eax
	mov dword[ebp - 8], eax
	jmp rotuloFimIf5
rotuloElse4:
	add esp, 4
	push dword[ebp - 8]
	push 2
	pop eax
	imul eax, dword [ESP]
	mov dword [ESP], eax
	pop eax
	mov dword[ebp - 8], eax
rotuloFimIf5: 	push dword[ebp - 8]
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
	push rotuloString6
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
	push rotuloString7
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
	jl rotuloFalsoREL8
	mov dword [ESP], 1
	jmp rotuloSaidaREL9
rotuloFalsoREL8: 	mov dword [ESP], 0
rotuloSaidaREL9: 	cmp dword[esp], 0
	je rotuloElse10
	add esp, 4
	push dword[ebp - 12]
	push @Integer
	call printf
	add esp, 8
	jmp rotuloFimIf11
rotuloElse10:
	add esp, 4
	push dword[ebp - 16]
	push @Integer
	call printf
	add esp, 8
rotuloFimIf11: 	push 1
	pop eax
	mov dword[ebp - 32], eax
rotuloWhile12: 	push dword[ebp - 32]
	push 10
	pop eax
	cmp dword [ESP], eax
	jg rotuloFalsoREL14
	mov dword [ESP], 1
	jmp rotuloSaidaREL15
rotuloFalsoREL14: 	mov dword [ESP], 0
rotuloSaidaREL15: 	cmp dword[esp], 0
	je rotuloFimWhile13
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
	jmp rotuloWhile12
rotuloFimWhile13: 	add esp, 4
	push 1
	pop eax
	mov dword[ebp - 32], eax
rotuloRepeat16: 	push dword[ebp - 32]
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
	jle rotuloFalsoREL17
	mov dword [ESP], 1
	jmp rotuloSaidaREL18
rotuloFalsoREL17: 	mov dword [ESP], 0
rotuloSaidaREL18: 	cmp dword[esp], 0
	je rotuloRepeat16
	add esp, 4
	push 1
	pop dword[ebp - 32]
	push 10
rotuloFOR19: 	push ecx
	mov ecx, dword[ebp - 32]
	cmp ecx, dword[esp+4]
	jg rotuloFIMFOR20
	pop ecx
	push dword[ebp - 32]
	push @Integer
	call printf
	add esp, 8
	add dword[ebp - 32], 1
	jmp rotuloFOR19
rotuloFIMFOR20: 	add esp, 8
	push rotuloString21
	call printf
	add esp, 4
	lea eax, [ebp - 24]
	push eax
	push @Integer
	call scanf
	add esp, 8
	push rotuloString22
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
	jle rotuloFalsoREL23
	mov dword [ESP], 1
	jmp rotuloSaidaREL24
rotuloFalsoREL23: 	mov dword [ESP], 0
rotuloSaidaREL24: 	push dword[ebp - 28]
	push 0
	pop eax
	cmp dword [ESP], eax
	jle rotuloFalsoREL25
	mov dword [ESP], 1
	jmp rotuloSaidaREL26
rotuloFalsoREL25: 	mov dword [ESP], 0
rotuloSaidaREL26: 	cmp dword [ESP + 4], 1
	jne rotuloFalsoMTL28
	pop eax
	cmp dword [ESP], eax
	jne rotuloFalsoMTL28
	mov dword [ESP], 1
	jmp rotuloSaidaMTL27
rotuloFalsoMTL28: 	mov dword [ESP], 0
rotuloSaidaMTL27: 	cmp dword[esp], 0
	je rotuloElse29
	add esp, 4
	push rotuloString31
	call printf
	add esp, 4
	push rotuloStringLN
	call printf
	add esp, 4
	jmp rotuloFimIf30
rotuloElse29:
	add esp, 4
	push rotuloString32
	call printf
	add esp, 4
	push rotuloStringLN
	call printf
	add esp, 4
rotuloFimIf30: 	leave
	ret

section .data

@Integer: db '%d',0
rotuloString6: db 'Valor: ',0
rotuloString7: db 'Resultado: ',0
rotuloString21: db 'Informe a: ',0
rotuloString22: db 'Informe b: ',0
rotuloString31: db 'Positivos',0
rotuloStringLN: db '',10,0
rotuloString32: db 'Um dos valores não é positivo',0
