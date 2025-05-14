# Dokument specifikace požadavků

## 1 Uživatelské rozhraní
    - Knihovna Bootstrap
    - Springboot Thymeleaf

## 2 Persistence oblíbených položek
    - Uživatelé jsou uloženi jednotlivě v databázi
    - Každý uživatel má přiřazeny svoje oblíbené položky

## 3 Definice dat a způsobu získávání dat
    - Data již dostupná se nestahují
    - Data jsou uložena v externí databázi s parametrem "isFavourite"

## 4 Mazání úložiště
    - Databáze automaticky maže data starší 14 dní

## 5 Externalizace konfigurace

## 6 Struktura JSON souborů


## 7 Definice endpointů
    - 
## 8 API
    - 
## 9 Evaluace zpráv

## 9.1 Hodnocení na škále <-10,10>
    - Každá zpráva chodí již s určitým "sentiment_score"
    - Skóre se zprůměrují a převedou na int na škále <-10,10>

## 10 Validace zpráv

## 11 Komunikace s modulem Burza v případě vícero klientů

## 12 Timeouty pro čtení z externích REST

## 13 Limity na zprávy
    - 