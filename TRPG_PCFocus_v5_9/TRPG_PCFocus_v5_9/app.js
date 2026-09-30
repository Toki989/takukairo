const pcs = [
  {name:'五色 探',reading:'ごしき たん',pl:'PLAYER 01',img:'assets/pcs/pc-1.png',san:'65',hp:'12',mp:'8',end:'生還'},
  {name:'火守 蓮',reading:'ひもり れん',pl:'PLAYER 02',img:'assets/pcs/pc-2.png',san:'47',hp:'10',mp:'11',end:'生還'},
  {name:'黒瀬 真',reading:'くろせ まこと',pl:'PLAYER 03',img:'assets/pcs/pc-3.png',san:'71',hp:'13',mp:'9',end:'生還'},
  {name:'赤城 迅',reading:'あかぎ じん',pl:'PLAYER 04',img:'assets/pcs/pc-4.png',san:'22',hp:'6',mp:'5',end:'継続'}
];
let current = 0;
const portrait = document.getElementById('mainPortrait');
const cards = [...document.querySelectorAll('.selector-card[data-index]')];
const addCard = document.querySelector('.selector-add');
const dots = [...document.querySelectorAll('.dots i')];
const selectorOffsets = [0, 28, 52, 76];
const selectorHeights = [94, 84, 77, 70];
function layoutSelectors(activeIndex){
  cards.forEach((card, i)=>{
    const distance = Math.abs(i - activeIndex);
    card.style.setProperty('--selector-offset', `${selectorOffsets[Math.min(distance, selectorOffsets.length - 1)]}px`);
    card.style.setProperty('--selector-height', `${selectorHeights[Math.min(distance, selectorHeights.length - 1)]}%`);
  });
  if(addCard){
    const addDistance = Math.abs(cards.length - activeIndex);
    const addOffset = Math.min(88, 24 + addDistance * 18);
    addCard.style.setProperty('--selector-offset', `${addOffset}px`);
  }
}
function selectPc(index){
  current = (index + pcs.length) % pcs.length;
  const pc = pcs[current];
  portrait.classList.add('is-changing');
  window.setTimeout(()=>{
    portrait.src = pc.img;
    portrait.alt = `${pc.name}の立ち絵`;
    document.getElementById('pcName').textContent = pc.name;
    document.getElementById('pcReading').textContent = pc.reading;
    document.getElementById('plName').textContent = pc.pl;
    document.getElementById('sanValue').textContent = pc.san;
    document.getElementById('hpValue').textContent = pc.hp;
    document.getElementById('mpValue').textContent = pc.mp;
    document.getElementById('endingValue').textContent = pc.end;
    cards.forEach((c,i)=>{
      const selected = i === current;
      c.classList.toggle('is-active', selected);
      c.setAttribute('aria-selected', String(selected));
      c.tabIndex = selected ? 0 : -1;
    });
    layoutSelectors(current);
    dots.forEach((d,i)=>d.classList.toggle('active',i===current));
    portrait.classList.remove('is-changing');
  },110);
}
cards.forEach((c,i)=>{
  c.tabIndex = i === current ? 0 : -1;
  c.addEventListener('click',()=>selectPc(Number(c.dataset.index)));
  c.addEventListener('keydown',(event)=>{
    let target = null;
    if(event.key === 'ArrowRight' || event.key === 'ArrowDown') target = (i + 1) % cards.length;
    if(event.key === 'ArrowLeft' || event.key === 'ArrowUp') target = (i - 1 + cards.length) % cards.length;
    if(event.key === 'Home') target = 0;
    if(event.key === 'End') target = cards.length - 1;
    if(target === null) return;
    event.preventDefault();
    selectPc(target);
    window.setTimeout(()=>cards[target].focus(),130);
  });
});
document.getElementById('prevPc').addEventListener('click',()=>selectPc(current-1));
document.getElementById('nextPc').addEventListener('click',()=>selectPc(current+1));

layoutSelectors(current);
