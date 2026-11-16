# MyRuneS MMO — OSRS V74

Snapshot do projeto solo local em 16/11/2026. Base Xeros 317 modificada. Interface em PT-BR; itens, NPCs e bosses mantêm os nomes em inglês.

## Jogar

Baixe `MyRuneS-MMO-V74-Portable.zip` na seção **Releases**, extraia a pasta inteira e abra **MyRuneS MMO.exe** no Windows. O Java necessário acompanha o pacote. Não é necessário usar um servidor externo.

A abertura inicia o mundo em segundo plano e mostra o login. Escolha um nome e uma senha próprios. O progresso é salvo automaticamente e ao fechar o cliente. Ferramentas e multiplicador de XP: **Meu mundo → Configurações e ferramentas**. Mudanças de XP valem na próxima abertura.

Este backup público inicia sem contas. Para preservar seus personagens existentes ao restaurar, guarde e recoloque sua pasta privada `runtime/server/save_files`, sem publicá-la no GitHub. O pacote público não contém personagens, senhas lembradas, logs, backups pessoais ou histórico Git antigo.

## Estado incluído

- V74: login único, fontes vetoriais, escala proporcional, ferramentas opcionais e `::charge shadow max`.
- V73: retaliação automática com Shadow, troca de formas de Kolodion e verificação ON/OFF em Guard.
- Conteúdo anterior da instalação: skills, equipamentos, bosses, raids, quests e diários disponíveis nesta base. Não representa implementação integral de todo o OSRS oficial; consulte `docs/PENDENCIAS-OSRS-V72.json` e os relatórios.
- Catálogo local de comandos: `docs/COMANDOS.html`.

```text
::charge shadow max
::item 566 40000
::item 562 100000
::charge shadow
::item 565 60000
::item 22446 200
::charge scythe
```

O cheat da Shadow completa 20.000 cargas sem materiais; os ataques continuam consumindo-as. `::charge scythe max` não está implementado nesta versão.

## Fontes e restauração

`Client/`, `Server/` e `Data/` preservam os fontes e dados da instalação V74. `launcher/` contém os iniciadores. O pacote da release inclui os binários atualmente jogáveis, bibliotecas, cache e Java. Baixe a release para restaurar também esses componentes, pois o clone Git não inclui o cache do cliente. Os relatórios de validação estão em `docs/`.

Os scripts de desenvolvimento herdados podem pressupor caminhos locais. O snapshot jogável é a referência da entrega; uma compilação integral da base desde um ambiente vazio não foi certificada nesta publicação.

## Origem e licença

Derivado de [hadefuwa/solak-xeros-317](https://github.com/hadefuwa/solak-xeros-317). Commit de origem em `UPSTREAM.json`; documentação original em `README-BASE-XEROS.md`. Licença GPL-3.0 conforme `LICENSE`; avisos e referências de terceiros mantidos. RuneScape e seus nomes pertencem aos respectivos titulares. Projeto comunitário sem afiliação com a Jagex.

## RS3

O RS3 é um projeto separado em desenvolvimento e não está incluído neste repositório do OSRS.
