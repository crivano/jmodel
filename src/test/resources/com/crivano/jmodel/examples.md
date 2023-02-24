# Examples

One of the best ways to understad the template engine is to see many examples of what can be accomplished.

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
    [@print country/].</p>
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
    [@if country == 'Brazil' depend='country']didn't
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
  [@if country == 'Brazil' depend='country']
    [@field var='state' options='Rio de Janeiro;São Paulo'/]
  [/@if]
[/@interview]

[@document]
  <p>Country:
    [@value var='country' options='Brazil;Argentina' refresh='country'/]</p>
  [@if country == 'Brazil' depend='country']
    <p>State:
      [@value var='state' options='Rio de Janeiro;São Paulo'/]</p>
  [/@if]
[/@document]
```

### Duplicated fields

Duplicated fields should be omitted from the interview:

```Markdown
Me, {name}, state that ...

Signed by: {name}
```

```FreeMarker
[@interview]
  [@field var='name'/]
[/@interview]

[@document]
  <p>Me,
    [@value var='name'/], state that ...</p>
  <p>Signed by:
    [@value var='name'/]</p>
[/@document]
```

### For

A For statement exemple that asks the number of people, then the name of each person:

```Markdown
Number of people: {field var='num' options='1;2;3;4;5' refresh='num'}

{for num depend='num'}
Name: {field var='name' index=num}
{/for}
```

```FreeMarker
[@interview]
  [@field var='num' options='1;2;3;4;5' refresh='num'/]
  [@for num depend='num']
    [@field var='name' index=num/]
  [/@for]
[/@interview]

[@document]
  <p>Number of people:
    [@value var='num' options='1;2;3;4;5' refresh='num'/]</p>
  [@for num depend='num']
    <p>Name:
      [@value var='name' index=num/]</p>
  [/@for]
[/@document]
```

### For with duplicated fields

A For statement that has duplicated fields within the loop:

```Markdown
Number of people: {field var='num' options='1;2;3;4;5' refresh='num'}

{for num depend='num'}
Name: {field var='name' index=num}

Name again: {field var='name' index=num}
{/for}
```

```FreeMarker
[@interview]
  [@field var='num' options='1;2;3;4;5' refresh='num'/]
  [@for num depend='num']
    [@field var='name' index=num/]
  [/@for]
[/@interview]

[@document]
  <p>Number of people:
    [@value var='num' options='1;2;3;4;5' refresh='num'/]</p>
  [@for num depend='num']
    <p>Name:
      [@value var='name' index=num/]</p>
    <p>Name again:
      [@value var='name' index=num/]</p>
  [/@for]
[/@document]
```

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
