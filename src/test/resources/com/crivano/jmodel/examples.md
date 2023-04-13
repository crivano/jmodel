# Examples

One of the best ways to understand the template engine is to see many examples of what can be accomplished.

### Single auto-field

A very basic example:

```Markdown
Hi {name}!
```

```FreeMarker
[@interview]
  [@field var='name'/]
[/@interview]

[@document]
  <p>Hi
    [@value var='name'/]!</p>
[/@document]
```

### Single Freemarker field

The same example, but using Freemarker field:

```Markdown
Hi [@field var='name'/]!
```

```FreeMarker
[@interview]
  [@field var='name'/]
[/@interview]

[@document]
  <p>Hi
    [@value var='name'/]!</p>
[/@document]
```

### Single selection field

A single selection field:

```Markdown
Hi {field var='name' options='Foo;Bar'}!
```

```FreeMarker
[@interview]
  [@field var='name' options='Foo;Bar'/]
[/@interview]

[@document]
  <p>Hi
    [@value var='name' options='Foo;Bar'/]!</p>
[/@document]
```

### Single selection auto-field

A single selection field:

```Markdown
Hi {name options='Foo;Bar'}!
```

```FreeMarker
[@interview]
  [@field var='name' options='Foo;Bar'/]
[/@interview]

[@document]
  <p>Hi
    [@value var='name' options='Foo;Bar'/]!</p>
[/@document]
```

### Two selection fields

Two selection fields:

```Markdown
Country: {field var='country' optios='Brazil;Argentina' refresh='country'}

Gender: {field var='gender' optios='Male;Female' refresh='gender'}
```

```FreeMarker
[@interview]
  [@field var='country' optios='Brazil;Argentina' refresh='country'/]
  [@field var='gender' optios='Male;Female' refresh='gender'/]
[/@interview]

[@document]
  <p>Country:
    [@value var='country' optios='Brazil;Argentina' refresh='country'/]</p>
  <p>Gender:
    [@value var='gender' optios='Male;Female' refresh='gender'/]</p>
[/@document]
```

### Print command

One auto-field and one print command:

```Markdown
I live in {country} and I love {print country}.
```

```FreeMarker
[@interview]
  [@field var='country'/]
[/@interview]

[@document]
  <p>I live in
    [@value var='country'/] and I love
    [@print expr=(country)/].</p>
[/@document]
```

### If not repositioned

An If statement that does not require repositioning:

```Markdown
Country: {field var='country' options='Brazil;Argentina' refresh='country'}

You {if country == 'Brazil' depend='country'}didn't{/if} win the WorldCup!
```

```FreeMarker
[@interview]
  [@field var='country' options='Brazil;Argentina' refresh='country'/]
[/@interview]

[@document]
  <p>Country:
    [@value var='country' options='Brazil;Argentina' refresh='country'/]</p>
  <p>You
    [@if expr=(country == 'Brazil') depend='country']didn't
    [/@if] win the WorldCup!</p>
[/@document]
```

### If repositioned

If statement that is repositioned to be outside of a paragraph:

```Markdown
Country: {field var='country' options='Brazil;Argentina' refresh='country'}

{if country == 'Brazil' depend='country'}State: {field var='state' options='Rio de Janeiro;São Paulo'}{/if}

```

```FreeMarker
[@interview]
  [@field var='country' options='Brazil;Argentina' refresh='country'/]
  [@if expr=(country == 'Brazil') depend='country']
    [@field var='state' options='Rio de Janeiro;São Paulo'/]
  [/@if]
[/@interview]

[@document]
  <p>Country:
    [@value var='country' options='Brazil;Argentina' refresh='country'/]</p>
  [@if expr=(country == 'Brazil') depend='country']
    <p>State:
      [@value var='state' options='Rio de Janeiro;São Paulo'/]</p>
  [/@if]
[/@document]
```

### Duplicated fields

Duplicated fields should be omitted from the interview:

```Markdown
I, {name}, born in ...

Signed by: {name}
```

```FreeMarker
[@interview]
  [@field var='name'/]
[/@interview]

[@document]
  <p>I,
    [@value var='name'/], born in ...</p>
  <p>Signed by:
    [@value var='name'/]</p>
[/@document]
```

### For

A For statement exemple that asks the number of people, then the name of each person:

```Markdown
Number of people: {field var='num' options='1;2;3;4;5' refresh='num'}

{for num depend='num'}
Name: {field var='name'}
{/for}
```

```FreeMarker
[@interview]
  [@field var='num' options='1;2;3;4;5' refresh='num'/]
  [@for expr=(num) depend='num' ; index]
    [@field var='name'/]
  [/@for]
[/@interview]

[@document]
  <p>Number of people:
    [@value var='num' options='1;2;3;4;5' refresh='num'/]</p>
  [@for expr=(num) depend='num' ; index]
    <p>Name:
      [@value var='name'/]</p>
  [/@for]
[/@document]
```

### For with duplicated fields

A For statement that has duplicated fields within the loop:

```Markdown
Number of people: {field var='num' options='1;2;3;4;5' refresh='num'}

{for num depend='num'}
Name: {field var='name' index=num}

Name again: {field var='name' index=index}
{/for}
```

```FreeMarker
[@interview]
  [@field var='num' options='1;2;3;4;5' refresh='num'/]
  [@for expr=(num) depend='num' ; index]
    [@field var='name' index=num/]
    [@field var='name' index=index/]
  [/@for]
[/@interview]

[@document]
  <p>Number of people:
    [@value var='num' options='1;2;3;4;5' refresh='num'/]</p>
  [@for expr=(num) depend='num' ; index]
    <p>Name:
      [@value var='name' index=num/]</p>
    <p>Name again:
      [@value var='name' index=index/]</p>
  [/@for]
[/@document]
```


### For repositioned

A For statement that is repositioned to outside of a table row:

```Markdown
Number of people: {field var='num' options='1;2;3;4;5' refresh='num'}

|Name|Gender|Age|
|----|------|---|
|{for num depend='num'}{field var='name'}|{field var='gender'}|{field var='age'}{/for}|
```

```FreeMarker
[@interview]
  [@field var='num' options='1;2;3;4;5' refresh='num'/]
  [@for expr=(num) depend='num' ; index]
    [@field var='name'/]
    [@field var='gender'/]
    [@field var='age'/]
  [/@for]
[/@interview]

[@document]
  <p>Number of people:
    [@value var='num' options='1;2;3;4;5' refresh='num'/]</p>
  <table>
    <thead>
      <tr>
        <th>Name</th>
        <th>Gender</th>
        <th>Age</th>
      </tr>
    </thead>
    <tbody>
      [@for expr=(num) depend='num' ; index]
        <tr>
          <td>
            [@value var='name'/]</td>
          <td>
            [@value var='gender'/]</td>
          <td>
            [@value var='age'/]</td>
        </tr>
      [/@for]
    </tbody>
  </table>
[/@document]
```

### For repositioned 2

A For statement that is repositioned to outside of a table row:

```Markdown
Total de fornecedores consultados: {quantidadeDeFornecedoresConsultados options='1;2;3;4;5;6;7;8;9;10' title='Total de Fornecedores Consultados' refresh='quantidadeDeFornecedoresConsultados' hint='Informar a quantidade de fornecedores consultados, ainda que tenham declinado em fornecer a proposta ou mesmo não tenham respondido aos e-mails (anexar todas as consultas e respostas a este Mapa). A quantidade mínima de propostas obtidas é de 3, porém, quanto mais fornecedores pesquisados, maior é a tendência de obtenção de melhores preços.'}

A pesquisa de preços resultou na seleção do fornecedor {nomeFornecedorSelecionado title='Fornecedor Selecionado' col='col-6'}, CNPJ {cnpjFornecedorSelecionado title='CNPJ do Fornecedor Selecionado' col='col-6'}, pois apresentou o menor valor pesquisado.

|Fornecedor consultado|Data da Pesquisa|Valor Pesquisado|
|:--------------------|:--------------:|---------------:|
|{for quantidadeDeFornecedoresConsultados depend='quantidadeDeFornecedoresConsultados'}{nomeFornecedor title=_index+') Fornecedor Consultado' col='col-4'}|{dtPesquisa title='Data da Pesquisa' col='col-4'}|{valPesquisado title='Valor Pesquisado' col='col-4'}{/for}|

```

```FreeMarker
[@interview]
  [@field var='num' options='1;2;3;4;5' refresh='num'/]
  [@for expr=(num) depend='num' ; index]
    [@field var='name'/]
    [@field var='gender'/]
    [@field var='age'/]
  [/@for]
[/@interview]

[@document]
  <p>Number of people:
    [@value var='num' options='1;2;3;4;5' refresh='num'/]</p>
  <table>
    <thead>
      <tr>
        <th>Name</th>
        <th>Gender</th>
        <th>Age</th>
      </tr>
    </thead>
    <tbody>
      [@for expr=(num) depend='num' ; index]
        <tr>
          <td>
            [@value var='name'/]</td>
          <td>
            [@value var='gender'/]</td>
          <td>
            [@value var='age'/]</td>
        </tr>
      [/@for]
    </tbody>
  </table>
[/@document]
```

### Constant

If the variable name is all in upper case, then it is treated as an constant, and a ```print``` command is issued:

```Markdown Document
Hi {SIGNEE_NAME}!
```

```FreeMarker
[@document]
  <p>Hi
    [@print expr=(SIGNEE_NAME)/]!</p>
[/@document]
```

### Grouping fields in the interview

A ```@group``` command may be used to organize and line up fields in the ```@interview``` section. A group may be used to present a caption or a warning:

```Markdown Document
{group title='Identification' warning='Your personal data is secure.'}
Hi {name}!
{/group}
```

```FreeMarker
[@interview]
  [@group title='Identification' warning='Your personal data is secure.']
    [@field var='name'/]
  [/@group]
[/@interview]

[@document]
  <p>Hi
    [@value var='name'/]!</p>
[/@document]
```

### Simple model with variable assigns

In certain situations it will be necessary to set global variables that can be used to customize the ```@document``` macro. For instance,
one may use a variable to choose a special style for the document, like Memorandum or Letter.

```Markdown Document
{set STYLE='LETTER'}

Hi {name}!
```

```FreeMarker
[#assign STYLE='LETTER'/]

[@interview]
  [@field var='name'/]
[/@interview]

[@document]
  <p>Hi
    [@value var='name'/]!</p>
[/@document]
```

Some variables are defined as part of the JModel specification:

Variable|Objective
---|---
STYLE|Defines the style of the document but the possible values shoud be defined by the application
PAGE_SIZE|Defines the size of the paper and the default value is 'A4'
PAGE_ORIENTATION|Defines the page orientation, default value is 'portrait' but can also be set to 'landscape'
MARGIN_LEFT|Defines the left margin, default value is '3cm'
MARGIN_RIGHT|Defines the right margin, default value is '2cm'
MARGIN_TOP|Defines the top margin, default value is '1cm'
MARGIN_BOTTOM|Defines the bottom margin, default value is '2cm'

## Models With Description

### Simple model with description

The description can be specified as an additional MarkDown text. In this case, the interview should include 
fields from both the description and the document. An additional ```[@description][/@description]``` 
should be created. 

```Markdown Description
{kind} document for {name}
```

```Markdown Document
Hi {name}!
```

```FreeMarker
[@interview]
  [@field var='kind'/]
  [@field var='name'/]
[/@interview]

[@description]
  <p>
    [@value var='kind'/] document for
    [@value var='name'/]</p>
[/@description]

[@document]
  <p>Hi
    [@value var='name'/]!</p>
[/@document]
```

### Description in the same template as the document

Sometimes it is better to have the description within the same template. It's possible by using the ```{description}``` command. 

```Markdown Document
{description}
{kind} document for {name}
{/description}

Hi {name}!
```

```FreeMarker
[@interview]
  [@field var='kind'/]
  [@field var='name'/]
[/@interview]

[@description]
  <p>
    [@value var='kind'/] document for
    [@value var='name'/]</p>
[/@description]

[@document]
  <p>Hi
    [@value var='name'/]!</p>
[/@document]
```

### Hooks

By using the ```{hook}``` command, it is possible do add a freemarker code that will run in specific moments of the document's life cicle. It's useful for situations when the model is used for
doing validations, initializing the workflow and so on. 

```Markdown Document
Hi {name}!

{hook 'AFTER_SIGN'}
[#assign wf=workflow.initialize('My WF Definition')/]
{/hook}
```

```FreeMarker
[@interview]
  [@field var='name'/]
[/@interview]

[@document]
  <p>Hi
    [@value var='name'/]!</p>
[/@document]

[@hook expr=('AFTER_SIGN')]
  [#assign wf=workflow.initialize('My WF Definition')/]
[/@hook]
```

JModel defines some possible values for the ```hook``` parameter:

Parameter|Life Cycle Event
---|---
BEFORE_SAVE|Just before the draft is saved
AFTER_DRAFT|When the draft is ready for signing
BEFORE_SIGN|Just before the signature is registered
AFTER_SIGN|Just after the signature is registered