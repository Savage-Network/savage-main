### Atualização (30/03/2026) <:spark:1420169721285709934>

```text
Core-Bungee:
- Implementado sistema completo de proteção anti-DDoS com rate limiting e blacklist automática
- Adicionados comandos administrativos para gerenciamento de blacklist (/antiddos)
- Sistema de monitoramento de conexões por IP com limites configuráveis
- Proteção contra ataques de força bruta com bloqueio temporário automático
- Interface de estatísticas em tempo real do sistema de proteção

Ajustes Gerais:
- Removida dependência circular entre módulos core e core-bungee
- Corrigido problemas de compilação no sistema de proteção anti-DDoS
- Padronizados comandos administrativos seguindo padrão visual do projeto
- Implementado sistema de logging para auditoria de ações administrativas
- Melhorada validação de endereços IP nos comandos de blacklist

Sistema de Comandos:
- /antiddos blacklist (ip) [motivo] - Bloqueia endereço IP manualmente
- /antiddos unblacklist (ip) - Remove bloqueio de endereço IP
- /antiddos stats - Exibe estatísticas do sistema de proteção
- /antiddos reload - Recarrega configurações do sistema

Melhorias de Segurança:
- Detecção automática de padrões suspeitos de conexão
- Limitação de tentativas de conexão por minuto por IP
- Sistema de blacklist com duração configurável
- Proteção contra nomes de usuário suspeitos
- Monitoramento contínuo de atividades anormais
```

@here
