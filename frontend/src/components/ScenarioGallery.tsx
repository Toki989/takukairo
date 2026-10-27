import {useLayoutEffect, useRef, useState, type SyntheticEvent} from 'react';
import {Link} from 'react-router-dom';
import type {Scenario} from '../api/types';
import {ScenarioImage} from './Images';
import styles from './ScenarioGallery.module.css';

interface GalleryProps {
  scenarios: Scenario[];
  loading: boolean;
  onFavorite: (scenario: Scenario) => Promise<void>;
}

export function ScenarioGallery({scenarios, loading, onFavorite}: GalleryProps) {
  return <div className={'gallery scenario-gallery ' + styles.gallery} aria-busy={loading}>
    {scenarios.map(scenario => <ScenarioWork key={scenario.id} scenario={scenario} onFavorite={onFavorite}/>)}
  </div>;
}

function ScenarioWork({scenario, onFavorite}: Pick<GalleryProps, 'onFavorite'> & {scenario: Scenario}) {
  const [shape, setShape] = useState<'portrait' | 'square' | 'landscape'>('portrait');
  const element = useRef<HTMLElement>(null);
  const [rows, setRows] = useState(1);
  useLayoutEffect(() => {
    const work = element.current!;
    const grid = work.parentElement!;
    function fitRows() {
      const css = getComputedStyle(grid);
      const step = parseFloat(css.gridAutoRows);
      const gap = parseFloat(css.getPropertyValue('--work-gap'));
      const next = Math.ceil((work.getBoundingClientRect().height + gap) / step);
      if (Number.isFinite(next) && next > 0) setRows(current => current === next ? current : next);
    }
    fitRows();
    const observer = new ResizeObserver(fitRows);
    observer.observe(work);
    return () => observer.disconnect();
  }, []);
  function measure(event: SyntheticEvent<HTMLImageElement>) {
    const {naturalWidth, naturalHeight} = event.currentTarget;
    if (!naturalWidth || !naturalHeight) return;
    const ratio = naturalWidth / naturalHeight;
    // Layout thresholds, not saved Scenario data or prescribed image crops.
    setShape(ratio >= 1.2 ? 'landscape' : ratio <= 0.8 ? 'portrait' : 'square');
  }
  return <article ref={element} className={'gallery-item ' + styles.work} data-shape={shape} style={{gridRowEnd: 'span ' + rows}}>
    <Link className={styles.link} to={'/scenarios/' + scenario.id}>
      <div className={styles.art}><ScenarioImage scenario={scenario} onLoad={measure}/></div>
      <div className={styles.metadata}>
        <h2>{scenario.name}</h2>
        {scenario.authorName && <p className={styles.author}>{scenario.authorName}</p>}
      </div>
    </Link>
    <div className={styles.actions}>
      <small className={styles.supplement}>{scenario.tableCount} 卓</small>
      <button className={styles.favorite} aria-label={scenario.name + 'のお気に入り'} aria-pressed={scenario.favorite} onClick={() => void onFavorite(scenario)}>{scenario.favorite ? '★' : '☆'}</button>
    </div>
  </article>;
}
