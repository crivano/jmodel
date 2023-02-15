Ask the number of people, then the name of each person:

```Markdown
Number of people: {field var='num' options='1;2;3;4;5' refresh='num'}

{for num depend='num'}
Name: {field var='name' index=num}
{/for}
```

```FreeMarker
[@interview]
  [@field var='num' options='1;2;3;4;5' refresh='num'/]
  [@field var='name' index=num/]
[/@interview]

[@document]
  <p>Number of people:
    [@value var='num' options='1;2;3;4;5' refresh='num'/]</p>
  [@for num  depend='num']
    <p> Name:
      [@value var='name' index=num/] </p>
  [/@for]
[/@document]
```

But it is wrong because there should be a for-loop inside the interview, as well.